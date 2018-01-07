package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;

import java.util.List;

final class MapState {

    /**
     * Used for debugging.
     */
    static Coordinates[] PATH_TO_CHECK = null;

    /**
     * Used for debugging.
     */
    static int MAX_COMMON_PATH_LENGTH = 0;

    /**
     * Current Level.
     */
    private final @NotNull Level level;

    private final int playerPosition;
    private final int playerLine;
    private final int playerColumn;
    private final byte currentObject;
    private final int[] gridStrengths;
    private final int numberOfUnfilledElements;
    private final @NotNull CoordinatesLinkedItem path;

    MapState(@NotNull Level level,
             int playerPosition,
             int playerLine,
             int playerColumn,
             byte currentObject,
             int[] gridStrengths,
             int numberOfUnfilledElements,
             @NotNull CoordinatesLinkedItem path) {
        this.level = level;
        this.playerPosition = playerPosition;
        this.playerLine = playerLine;
        this.playerColumn = playerColumn;
        this.currentObject = currentObject;
        this.gridStrengths = gridStrengths;
        this.numberOfUnfilledElements = numberOfUnfilledElements;
        this.path = path;
    }

    /**
     * Process the current state
     *
     * @param nextStates next state to add the next possible positions
     * @return true if the current state is winning, false elsewhere
     */
    boolean processState(@NotNull LinkedList<MapState> nextStates) {
        if (
                (level.gridElements[playerPosition] == MapElement.EXIT_INDEX) &&
                        (numberOfUnfilledElements == 0)) {
            return true;
        } else {
            addAvailableDirections(nextStates);
            return false;
        }
    }

    private boolean canGo(int targetPosition) {
        if (gridStrengths[targetPosition] == 0) {
            return false;
        }
        byte targetPositionContent = level.gridElements[targetPosition];
        if (currentObject == MapElement.EMPTY_INDEX) {
            return MapElement.CAN_GO_HOLDING_NOTHING[targetPositionContent];
        } else {
            if (MapElement.CAN_GO_HOLDING_SOMETHING[targetPositionContent]) {
                return
                        MapElement.CAN_WALK_FREELY[targetPositionContent] ||
                                MapElement.HOLE_FOR_ELEMENT[targetPositionContent] == currentObject;
            } else {
                return false;
            }
        }
    }

    private @NotNull MapState createMapState(
            int targetPosition,
            int targetColumn,
            int targetLine
    ) {
        int[] targetGridStrengths = gridStrengths.clone();
        targetGridStrengths[targetPosition]--;
        byte targetCurrentObject = currentObject;
        byte targetPositionContent = level.gridElements[targetPosition];
        int targetNumberOfUnfilledElements = numberOfUnfilledElements;
        if (currentObject == MapElement.EMPTY_INDEX) {
            if (MapElement.CAN_BE_PICKED[targetPositionContent]) {
                targetCurrentObject = targetPositionContent;
            }
        } else {
            if (MapElement.HOLE_FOR_ELEMENT[targetPositionContent] == currentObject) {
                targetCurrentObject = MapElement.EMPTY_INDEX;
                targetNumberOfUnfilledElements--;
            }
        }
        if (MapElement.IS_TRIGGER[targetPositionContent]) {
            for (Integer basementTile : level.basementTiles[targetPositionContent]) {
                targetGridStrengths[basementTile] = 1;
            }
        }
        return new MapState(
                level,
                targetPosition,
                targetLine,
                targetColumn,
                targetCurrentObject,
                targetGridStrengths,
                targetNumberOfUnfilledElements,
                new CoordinatesLinkedItem(targetLine, targetColumn, path)
        );
    }

    private void addAvailableDirections(@NotNull LinkedList<MapState> nextStates) {
        // checkPathForDebug();
        {
            // up
            boolean isFirstLine = (playerLine == 0);
            int targetPositionUp = playerPosition - level.width;
            if ((!isFirstLine) && canGo(targetPositionUp)) {
                nextStates.add(createMapState(targetPositionUp, playerColumn, playerLine - 1));
            }
        }
        {
            // down
            boolean isLastLine = (playerLine == (level.height - 1));
            int targetPositionDown = playerPosition + level.width;
            if ((!isLastLine) && canGo(targetPositionDown)) {
                nextStates.add(createMapState(targetPositionDown, playerColumn, playerLine + 1));
            }

        }
        {
            // left
            boolean isFirstColumn = (playerColumn == 0);
            int targetPositionLeft = playerPosition - 1;
            if ((!isFirstColumn) && canGo(targetPositionLeft)) {
                nextStates.add(createMapState(targetPositionLeft, playerColumn - 1, playerLine));
            }

        }
        {
            // right
            boolean isLastColumn = (playerColumn == (level.width - 1));
            int targetPositionRight = playerPosition + 1;
            if ((!isLastColumn) && canGo(targetPositionRight)) {
                nextStates.add(createMapState(targetPositionRight, playerColumn + 1, playerLine));
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

            if (toLine == (fromLine + 1)) {
                direction = Direction.DOWN;
            } else if (toLine == (fromLine - 1)) {
                direction = Direction.UP;
            } else if (toColumn == (fromColumn - 1)) {
                direction = Direction.LEFT;
            } else if (toColumn == (fromColumn + 1)) {
                direction = Direction.RIGHT;
            }
            result[i] = coordinates.toString() + ((direction == -1) ? "" : (" " + Direction.toKey(direction)));

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
                if (character == MapElement.EXIT_INDEX) {
                    character = MapElement.EMPTY_INDEX;
                }
                lineChar[columnIndex] = character;
            }
            result[lineIndex] = lineChar;
        }

        Coordinates currentPoint = new Coordinates(level.entryLine, level.entryColumn);
        result[level.entryLine][level.entryColumn] = levelParser.elementsToChars.get(MapElement.ENTRY_INDEX);
        result[level.exitLine][level.exitColumn] = levelParser.elementsToChars.get(MapElement.EXIT_INDEX);

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

    private static @NotNull Coordinates coordinatesFromDirection(@NotNull Coordinates coordinates, int direction) {
        switch (direction) {
            case Direction.UP:
                return new Coordinates(coordinates.line - 1, coordinates.column);
            case Direction.DOWN:
                return new Coordinates(coordinates.line + 1, coordinates.column);
            case Direction.LEFT:
                return new Coordinates(coordinates.line, coordinates.column - 1);
            case Direction.RIGHT:
                return new Coordinates(coordinates.line, coordinates.column + 1);
            default:
                throw new RuntimeException("Unknown direction [" + direction + "]");
        }
    }
}
