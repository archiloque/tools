package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.archiloque.roofbot.MapElement.CAN_GO_HOLDING_NOTHING;
import static net.archiloque.roofbot.MapElement.CAN_WALK_FREELY;
import static net.archiloque.roofbot.MapElement.EMPTY_INDEX;
import static net.archiloque.roofbot.MapElement.ENTRY_INDEX;
import static net.archiloque.roofbot.MapElement.EXIT_INDEX;
import static net.archiloque.roofbot.MapElement.FAN_INDEX;
import static net.archiloque.roofbot.MapElement.GREEN_OBJECT_INDEX;
import static net.archiloque.roofbot.MapElement.HOLE_FOR_ELEMENT;
import static net.archiloque.roofbot.MapElement.RED_OBJECT_INDEX;
import static net.archiloque.roofbot.MapElement.TELEPORTER_1_INDEX;
import static net.archiloque.roofbot.MapElement.TELEPORTER_2_INDEX;
import static net.archiloque.roofbot.MapElement.TRIGGER_1_INDEX;
import static net.archiloque.roofbot.MapElement.TRIGGER_2_INDEX;

final class MapState {

    /**
     * Used for debugging.
     */
    private static Coordinates[] PATH_TO_CHECK = null;

    /**
     * Used for debugging.
     */
    private static int MAX_COMMON_PATH_LENGTH = 0;

    /**
     * Current Level.
     */
    private final @NotNull Level level;

    private final int playerPosition;
    private final byte currentObject;
    private final byte[] gridStrengths;
    private final byte numberOfUnfilledElements;
    private final @NotNull CoordinatesLinkedItem path;

    MapState(@NotNull Level level,
             int playerPosition,
             byte currentObject,
             byte[] gridStrengths,
             byte numberOfUnfilledElements,
             @NotNull CoordinatesLinkedItem path) {
        this.level = level;
        this.playerPosition = playerPosition;
        this.currentObject = currentObject;
        this.gridStrengths = gridStrengths;
        this.numberOfUnfilledElements = numberOfUnfilledElements;
        this.path = path;
    }

    private static @NotNull Coordinates coordinatesFromDirection(@NotNull Coordinates coordinates, int direction) {
        switch (direction) {
            case Direction.UP:
                return new Coordinates((byte) (coordinates.line - 1), coordinates.column);
            case Direction.DOWN:
                return new Coordinates((byte) (coordinates.line + 1), coordinates.column);
            case Direction.LEFT:
                return new Coordinates(coordinates.line, (byte) (coordinates.column - 1));
            case Direction.RIGHT:
                return new Coordinates(coordinates.line, (byte) (coordinates.column + 1));
            default:
                throw new RuntimeException("Unknown direction [" + direction + "]");
        }
    }

    /**
     * Process the current state
     *
     * @return true if the current state is winning, false elsewhere
     */
    boolean processState() {
        if (
                (level.gridElements[playerPosition] == EXIT_INDEX) &&
                        (numberOfUnfilledElements == 0)) {
            return true;
        } else {
            addAvailableDirections();
            return false;
        }
    }

    private @Nullable MapState tryToGoFan(
            byte direction,
            int targetPosition,
            byte[] targetGridStrengths,
            byte targetCurrentObject,
            byte targetNumberOfUnfilledElements,
            CoordinatesLinkedItem path
    ) {
        int newTargetPosition = -1;

        switch (direction) {
            case Direction.UP: {
                byte targetLineU = (byte) (targetPosition / level.width);
                if (targetLineU == 0) {
                    return null;
                } else {
                    newTargetPosition = targetPosition - level.width;
                    break;
                }
            }
            case Direction.DOWN:
                byte targetLineD = (byte) (targetPosition / level.width);
                if (targetLineD == (level.height - 1)) {
                    return null;
                } else {
                    newTargetPosition = targetPosition + level.width;
                    break;
                }
            case Direction.LEFT:
                byte targetColumnL = (byte) (targetPosition % level.width);
                if (targetColumnL == 0) {
                    return null;
                } else {
                    newTargetPosition = targetPosition - 1;
                    break;
                }
            case Direction.RIGHT:
                byte targetColumnR = (byte) (targetPosition % level.width);
                if (targetColumnR == (level.width - 1)) {
                    return null;
                } else {
                    newTargetPosition = targetPosition + 1;
                    break;
                }
            default:
                throw new RuntimeException("Unknown direction " + direction);
        }
        return tryToGo(
                direction,
                newTargetPosition,
                targetGridStrengths,
                targetCurrentObject,
                targetNumberOfUnfilledElements,
                new CoordinatesLinkedItem(targetPosition, path)
        );

    }

    private @Nullable MapState tryToGo(
            byte direction,
            int targetPosition,
            @Nullable byte[] targetGridStrengths,
            byte targetCurrentObject,
            byte targetNumberOfUnfilledElements,
            CoordinatesLinkedItem path
    ) {
        byte targetStrength = ((targetGridStrengths != null) ? targetGridStrengths : gridStrengths)[targetPosition];
        if (targetStrength == 0) {
            return null;
        }

        byte targetPositionContent = level.gridElements[targetPosition];
        if (targetCurrentObject == EMPTY_INDEX) {
            // we hold nothing
            if (!CAN_GO_HOLDING_NOTHING[targetPositionContent]) {
                return null;
            }
        } else {
            // we hold something
            if (!(CAN_WALK_FREELY[targetPositionContent] || (HOLE_FOR_ELEMENT[targetPositionContent] == currentObject))) {
                return null;
            }
        }

        if (targetGridStrengths == null) {
            targetGridStrengths = gridStrengths.clone();
        }
        targetGridStrengths[targetPosition] = (byte) (targetStrength - 1);

        if (targetCurrentObject == EMPTY_INDEX) {
            if ((targetPositionContent >= GREEN_OBJECT_INDEX) && (targetPositionContent <= RED_OBJECT_INDEX)) {
                targetCurrentObject = targetPositionContent;
            }
        } else {
            if (HOLE_FOR_ELEMENT[targetPositionContent] == targetCurrentObject) {
                targetCurrentObject = EMPTY_INDEX;
                targetNumberOfUnfilledElements--;
            }
        }

        if ((targetPositionContent == TRIGGER_1_INDEX) || (targetPositionContent == TRIGGER_2_INDEX)) {
            for (Integer basementTile : level.basementTiles[targetPositionContent]) {
                targetGridStrengths[basementTile] = (byte) 1;
            }
        } else if (targetPositionContent == FAN_INDEX) {
            return tryToGoFan(
                    direction,
                    targetPosition,
                    targetGridStrengths,
                    targetCurrentObject,
                    targetNumberOfUnfilledElements,
                    path);
        } else if (direction != Direction.TELEPORT) {
            if (targetPositionContent == TELEPORTER_1_INDEX) {
                Coordinates teleporterExit = level.teleporters.get(targetPosition);
                return tryToGo(
                        Direction.TELEPORT,
                        teleporterExit.line * level.width + teleporterExit.column,
                        targetGridStrengths,
                        targetCurrentObject,
                        targetNumberOfUnfilledElements,
                        new CoordinatesLinkedItem(targetPosition, path)
                );

            } else if (targetPositionContent == TELEPORTER_2_INDEX) {
                Coordinates teleporterExit = level.teleporters.get(targetPosition);
                return tryToGo(
                        Direction.TELEPORT,
                        teleporterExit.line * level.width + teleporterExit.column,
                        targetGridStrengths,
                        targetCurrentObject,
                        targetNumberOfUnfilledElements,
                        new CoordinatesLinkedItem(targetPosition, path)
                );
            }

        }
        return new MapState(
                level,
                targetPosition,
                targetCurrentObject,
                targetGridStrengths,
                targetNumberOfUnfilledElements,
                new CoordinatesLinkedItem(targetPosition, path)
        );
    }

    private static final byte[] DIRECTIONS = new byte[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

    private void addAvailableDirections() {
        // checkPathForDebug();
        int[] possibleDirections = level.possibleDirections[playerPosition];
        for (int i = 0; i < 4; i++) {
            int possiblePosition = possibleDirections[i];
            if (possiblePosition != -1) {
                MapState mapState = tryToGo(
                        DIRECTIONS[i],
                        possiblePosition,
                        null,
                        currentObject,
                        numberOfUnfilledElements,
                        path
                );
                if (mapState != null) {
                    level.addMapState(mapState);
                }
            }
        }
    }

    /**
     * Used for debugging
     */
    private void checkPathForDebug() {
        if (PATH_TO_CHECK == null) {
            throw new RuntimeException("Forgot to set PATH_TO_CHECK, or forgot to comment call to checkPathForDebug !");
        }
        List<Coordinates> pathAsList = path.getAsList(level);
        Coordinates[] pathAsArray = pathAsList.toArray(new Coordinates[pathAsList.size()]);
        int maxLength = Math.min(pathAsArray.length, PATH_TO_CHECK.length);
        for (int i = 0; i < maxLength; i++) {
            if (!pathAsArray[i].equals(PATH_TO_CHECK[i])) {
                MAX_COMMON_PATH_LENGTH = Math.max(MAX_COMMON_PATH_LENGTH, i - 1);
                return;
            }
        }
        MAX_COMMON_PATH_LENGTH = Math.max(MAX_COMMON_PATH_LENGTH, maxLength);
        if (MAX_COMMON_PATH_LENGTH == 3) {
            System.out.println("Break");
        }
    }

    @NotNull String[] printableGrid() {
        char[][] lines = printableGridAsCharArrays();
        String[] result = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = new String(lines[i]);
        }
        return result;
    }

    @NotNull String[] printablePath() {
        List<Coordinates> pathAsLIst = path.getAsList(level);
        Coordinates[] pathArrayCoordinates = pathAsLIst.toArray(new Coordinates[pathAsLIst.size()]);
        String[] result = new String[pathArrayCoordinates.length];

        int fromLine = -2;
        int fromColumn = -2;

        for (int i = 0; i < pathArrayCoordinates.length; i++) {
            Coordinates coordinates = pathArrayCoordinates[i];
            int toLine = coordinates.line;
            int toColumn = coordinates.column;

            int direction = -1;

            if ((toLine == (fromLine + 1)) && (toColumn == fromColumn)) {
                direction = Direction.DOWN;
            } else if ((toLine == (fromLine - 1)) && (toColumn == fromColumn)) {
                direction = Direction.UP;
            } else if ((fromLine == toLine) && (toColumn == (fromColumn - 1))) {
                direction = Direction.LEFT;
            } else if ((fromLine == toLine) && (toColumn == (fromColumn + 1))) {
                direction = Direction.RIGHT;
            }

            result[i] =
                    coordinates.toString() +
                            (((direction == -1) || (level.gridElements[fromLine * level.width + fromColumn] == FAN_INDEX)) ? "" : (" " + Direction.toKey(direction)));

            fromLine = coordinates.line;
            fromColumn = coordinates.column;

        }
        return result;
    }

    @NotNull
    private char[][] printableGridAsCharArrays() {
        LevelParser levelParser = new LevelParser();
        char[][] result = new char[level.height][];
        for (int lineIndex = 0; lineIndex < level.height; lineIndex++) {
            char[] lineChar = new char[level.width];
            for (int columnIndex = 0; columnIndex < level.width; columnIndex++) {
                int position = (lineIndex * level.width) + columnIndex;
                byte element = level.gridElements[position];
                Character character = levelParser.elementsToChars.get(element);
                if (character == null) {
                    throw new RuntimeException("Unknown element [" + element + "]");
                }
                if (character == EXIT_INDEX) {
                    character = EMPTY_INDEX;
                }
                lineChar[columnIndex] = character;
            }
            result[lineIndex] = lineChar;
        }

        Coordinates currentPoint = new Coordinates(level.entryLine, level.entryColumn);
        result[level.entryLine][level.entryColumn] = levelParser.elementsToChars.get(ENTRY_INDEX);
        result[level.exitLine][level.exitColumn] = levelParser.elementsToChars.get(EXIT_INDEX);

        List<Coordinates> pathAsLIst = path.getAsList(level);

        Coordinates[] pathArrayCoordinates = pathAsLIst.toArray(new Coordinates[pathAsLIst.size()]);
        for (int i = 0; i < pathArrayCoordinates.length - 1; i++) {
            Coordinates from = pathArrayCoordinates[i];
            int fromLine = from.line;
            int fromColumn = from.column;
            Coordinates to = pathArrayCoordinates[i + 1];
            int toLine = to.line;
            int toColumn = to.column;
            int direction;

            if (toLine == (fromLine + 1)) {
                direction = Direction.DOWN;
            } else if (toLine == (fromLine - 1)) {
                direction = Direction.UP;
            } else if (toColumn == (fromColumn - 1)) {
                direction = Direction.LEFT;
            } else {
                direction = Direction.RIGHT;
            }
            result[fromLine][fromColumn] = (i == 0) ? Direction.toCharEntry(direction) : Direction.toChar(direction);
            currentPoint = coordinatesFromDirection(currentPoint, direction);
        }

        for (int lineIndex = 0; lineIndex < level.height; lineIndex++) {
            for (int columnIndex = 0; columnIndex < level.width; columnIndex++) {
                int position = (level.width * lineIndex) + columnIndex;
                int strength = level.gridStrengths[position];
                if (strength > 1) {
                    result[lineIndex][columnIndex] = '?';
                }

            }
        }
        return result;
    }
}
