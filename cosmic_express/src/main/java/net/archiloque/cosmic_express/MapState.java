package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Represents a state of the level Map.
 */
final class MapState {

    static int[] TRAIN_PATH_TO_CHECK = null;

    static boolean FOUND_TRAIN_PATH = false;

    /**
     * Path of the train leading to the current state.
     */
    final @Nullable CoordinatesLinkedItem previousTrainPath;

    /**
     * Current Level.
     */
    private final @NotNull Level level;

    /**
     * False = position is empty before executing the state.
     */
    final @NotNull BitSet previousGrid;

    /**
     * Target of the current step step be ? -1 = we are exiting
     */
    private final int currentStepTargetCoordinates;

    /**
     * The train elements before executing the state
     */
    private final @NotNull TrainElement[] previousTrainElements;

    private final @NotNull BitSet previousGridSegment;

    private boolean enteredNewSegment = false;

    private int newMissingNumberOfMonsters;

    /**
     * Index of the map indicating where are the monsters ins.
     */
    private int monsterInsIndex;

    /**
     * Index of the map indicating where are the monsters outs.
     */
    private int monsterOutsIndex;

    /**
     * Map showing where are the monsters ins.
     */
    private int[][] monsterInsGrid;

    /**
     * Map showing where are the monsters outs.
     */
    private int[][] monsterOutsGrid;

    /**
     * Coordinates of the exit.
     */
    int exitCoordinates;

    MapState(
            @NotNull Level level,
            @NotNull BitSet previousGrid,
            @NotNull BitSet previousGridSegment,
            int previousNumberOfMonsters,
            int currentStepTargetCoordinates,
            @NotNull TrainElement[] previousTrainElements,
            @Nullable CoordinatesLinkedItem previousTrainPath,
            int exitCoordinates,
            int monsterInsIndex,
            int[][] monsterInsGrid,
            int monsterOutsIndex,
            int[][] monsterOutsGrid

    ) {
        this.level = level;
        this.previousGrid = previousGrid;
        this.previousGridSegment = previousGridSegment;
        this.previousTrainPath = previousTrainPath;
        this.currentStepTargetCoordinates = currentStepTargetCoordinates;
        this.previousTrainElements = previousTrainElements;
        this.newMissingNumberOfMonsters = previousNumberOfMonsters;
        this.exitCoordinates = exitCoordinates;

        this.monsterInsIndex = monsterInsIndex;
        this.monsterInsGrid = monsterInsGrid;
        this.monsterOutsIndex = monsterOutsIndex;
        this.monsterOutsGrid = monsterOutsGrid;
    }

    boolean processState(@NotNull LinkedList<MapState> nextStates) {
        if (currentStepTargetCoordinates == exitCoordinates) {
            boolean isTrainEmpty = true;
            for (int i = 1; isTrainEmpty && (i < level.trainSize); i++) {
                isTrainEmpty = (previousTrainElements[i].content == TrainElementContent.NO_CONTENT);
            }

            return (newMissingNumberOfMonsters == 0) && isTrainEmpty;
        }
        BitSet newGrid = (BitSet) previousGrid.clone();

        TrainElement[] trainElements = processTrainElements(newGrid);
        if (trainElements == null) {
            return false;
        }

        // running normally
        @Nullable CoordinatesLinkedItem trainPath = createTrainPath();
        addAvailableDirections(newGrid, trainElements, trainPath, nextStates);
        return false;
    }

    private @NotNull MapState createMapState(
            @NotNull BitSet grid,
            @NotNull BitSet gridCurrentSegment,
            @NotNull TrainElement[] trainElements,
            @Nullable CoordinatesLinkedItem trainPath,
            int targetCoordinates) {
        return new MapState(
                level,
                grid,
                gridCurrentSegment,
                newMissingNumberOfMonsters,
                targetCoordinates,
                trainElements,
                trainPath,
                exitCoordinates,
                monsterInsIndex,
                monsterInsGrid,
                monsterOutsIndex,
                monsterOutsGrid);
    }

    private @Nullable CoordinatesLinkedItem createTrainPath() {
        if (currentStepTargetCoordinates != -1) {
            return new CoordinatesLinkedItem(currentStepTargetCoordinates, previousTrainPath);
        } else {
            return previousTrainPath;
        }
    }

    /**
     * Process the train elements
     *
     * @param newGrid the current grid.
     * @return the new train elements or null if the move is bad and we should cancel it.
     */
    private @Nullable TrainElement[] processTrainElements(@NotNull BitSet newGrid) {
        TrainElement[] trainElements = new TrainElement[level.trainSize];
        for (int trainElementIndex = 0; trainElementIndex < level.trainSize; trainElementIndex++) {
            TrainElement trainElement = processTrainElement(newGrid, trainElementIndex);
            if (trainElement != null) {
                trainElements[trainElementIndex] = trainElement;
            } else {
                return null;
            }
        }
        return trainElements;
    }

    private @Nullable TrainElement processTrainElement(@NotNull BitSet newGrid, int trainElementIndex) {
        TrainElement previousTrainElement = previousTrainElements[trainElementIndex];

        // Should still wait ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.WAITING) {
            if (
                    (trainElementIndex != 0) &&
                            (previousTrainElements[trainElementIndex - 1].trainElementStatus == TrainElementStatus.WAITING)
                    ) {
                return previousTrainElement;
            }
        }

        int newTrainElementCoordinates =
                calculateNewCoordinates(previousTrainElement, trainElementIndex, currentStepTargetCoordinates);
        int newTrainElementLocalCoordinates =
                ((newTrainElementCoordinates >> 16) * level.width) + (newTrainElementCoordinates & 65535);

        byte newTrainElementContent = previousTrainElement.content;

        // No content for head
        if (trainElementIndex != 0) {
            if (newTrainElementContent != TrainElementContent.NO_CONTENT) {
                int[] monsterOuts = monsterOutsGrid[newTrainElementLocalCoordinates];
                if (canEmpty(monsterOuts, newTrainElementContent)) {
                    newTrainElementContent = TrainElementContent.NO_CONTENT;
                    enteredNewSegment = true;
                }
            }

            if (newTrainElementContent == TrainElementContent.NO_CONTENT) {
                int[] monsterIns = monsterInsGrid[newTrainElementLocalCoordinates];
                if ((monsterIns != null) && (monsterIns.length == 1)) {
                    int monsterInCoordinates = monsterIns[0];
                    int element = level.grid[monsterInCoordinates];
                    newTrainElementContent = MapElement.MONSTER[element];
                    monsterInsIndex -= 1 << Arrays.binarySearch(level.monsterIns, monsterInCoordinates);
                    monsterInsGrid = level.monsterInsGrids[monsterInsIndex];
                    enteredNewSegment = true;
                }
            }
        }

        return new TrainElement(TrainElementStatus.RUNNING,
                newTrainElementContent,
                newTrainElementCoordinates);
    }

    private boolean canEmpty(
            @Nullable int[] monsterOuts,
            byte trainElementContent) {
        if (monsterOuts != null) {
            for (int monsterOutCoordinates : monsterOuts) {
                int monsterOutElement = level.grid[monsterOutCoordinates];
                if (trainElementContent == monsterOutElement) {
                    newMissingNumberOfMonsters--;
                    monsterOutsIndex -= 1 << Arrays.binarySearch(level.monsterOuts, monsterOutCoordinates);
                    monsterOutsGrid = level.monsterOutsGrids[monsterOutsIndex];
                    return true;
                }
            }
        }
        return false;
    }

    private int calculateNewCoordinates(
            @NotNull TrainElement trainElement,
            int trainElementIndex,
            int targetPosition) {
        if (trainElement.trainElementStatus == TrainElementStatus.WAITING) {
            // Entering the board
            return level.entry;
        } else if (trainElementIndex == 0) {
            return targetPosition;
        } else {
            return previousTrainElements[trainElementIndex - 1].coordinates;
        }
    }

    private void addAvailableDirections(
            @NotNull BitSet grid,
            @NotNull TrainElement[] trainElements,
            @Nullable CoordinatesLinkedItem trainPath,
            @NotNull LinkedList<MapState> nextStates) {

        int trainHeadCoordinates = trainElements[0].coordinates;
        int currentLine = trainHeadCoordinates >> 16;
        int currentColumn = trainHeadCoordinates & 65535;
        int currentPosition = (currentLine * level.width) + currentColumn;

        grid.set(currentPosition);

        // checkTrainPathForDebug(trainPath);

        BitSet newGridCurrentSegment = enteredNewSegment ?
                new BitSet(level.width * level.height) :
                ((BitSet) previousGridSegment.clone());
        newGridCurrentSegment.set(currentPosition);

        boolean isFirstLine = (currentLine == 0);
        boolean isLastLine = (currentLine == (level.height - 1));
        boolean isFirstColumn = (currentColumn == 0);
        boolean isLastColumn = (currentColumn == (level.width - 1));

        boolean isANewSegmentOrNearAMonsterInOrOut = enteredNewSegment;
        for (int i = 0; (!isANewSegmentOrNearAMonsterInOrOut) && (i < level.trainSize - 1); i++) {
            int currentTrainElementCoordinates = trainElements[i].coordinates;
            if (currentTrainElementCoordinates != -1) {
                int currentTrainElementLocalCoordinates = ((currentTrainElementCoordinates >> 16) * level.width) + (currentTrainElementCoordinates & 65535);
                isANewSegmentOrNearAMonsterInOrOut =
                        (monsterInsGrid[currentTrainElementLocalCoordinates] != null) ||
                                (monsterOutsGrid[currentTrainElementLocalCoordinates] != null);
            }
        }
        {
            // up
            int targetPositionUp = currentPosition - level.width;
            if ((!isFirstLine) &&
                    (!grid.get(targetPositionUp))) {
                if (isANewSegmentOrNearAMonsterInOrOut ||
                        (
                                ((currentLine == 1) || (!previousGridSegment.get(targetPositionUp - level.width))) && // up
                                        (isFirstColumn || (!previousGridSegment.get(targetPositionUp - 1)) && // left
                                                (isLastColumn || (!previousGridSegment.get(targetPositionUp + 1))) // right
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, ((currentLine - 1) << 16) + currentColumn));
                }
            }
        }

        {
            // down
            int targetPositionDown = currentPosition + level.width;
            if ((!isLastLine) &&
                    (!grid.get(targetPositionDown))) {
                if (isANewSegmentOrNearAMonsterInOrOut ||
                        (
                                ((currentLine == (level.height - 2)) || (!previousGridSegment.get(targetPositionDown + level.width))) && // down
                                        (isFirstColumn || (!previousGridSegment.get(targetPositionDown - 1)) && // left
                                                (isLastColumn || (!previousGridSegment.get(targetPositionDown + 1))) // right
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, ((currentLine + 1) << 16) + currentColumn));
                }
            }
        }

        {
            // left
            int targetPositionLeft = currentPosition - 1;
            if ((!isFirstColumn) &&
                    (!grid.get(targetPositionLeft))) {
                if (isANewSegmentOrNearAMonsterInOrOut ||
                        (
                                (isFirstLine || (!previousGridSegment.get(targetPositionLeft - level.width))) && // up
                                        (isLastLine || (!previousGridSegment.get(targetPositionLeft + level.width))) && // down
                                        ((currentColumn == 1) || (!previousGridSegment.get(targetPositionLeft - 1))  // left
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, (currentLine << 16) + currentColumn - 1));
                /*} else {
                    @NotNull String[] gridToPrint = bitSetToStringGrid(previousGridSegment);
                    gridToPrint[currentLine] =
                            gridToPrint[currentLine].substring(0, currentColumn) +
                                    'X' +
                                    ((currentColumn == (level.width - 1)) ? "" : gridToPrint[currentLine].substring(currentColumn + 1));
                    System.out.println(String.join("\n", gridToPrint));
                    System.out.println("");*/
                }
            }
        }

        {
            // right
            int targetPositionRight = currentPosition + 1;
            if ((!isLastColumn) &&
                    (!grid.get(targetPositionRight))) {
                if (isANewSegmentOrNearAMonsterInOrOut ||
                        (
                                (isFirstLine || (!previousGridSegment.get(targetPositionRight - level.width))) && // up
                                        (isLastLine || (!previousGridSegment.get(targetPositionRight + level.width))) && // down
                                        ((currentColumn == (level.width - 2)) || (!previousGridSegment.get(targetPositionRight + 1))  // right
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, (currentLine << 16) + currentColumn + 1));
                }
            }
        }
    }

    /**
     * Used for debugging
     */
    private void checkTrainPathForDebug(@Nullable CoordinatesLinkedItem trainPath) {
        if ((TRAIN_PATH_TO_CHECK != null) && (!FOUND_TRAIN_PATH)) {
            List<Integer> trainPathAsArray = (trainPath == null) ? new ArrayList<>() : trainPath.getAsList(level);
            int[] currentPathArray = Level.listToPrimitiveIntArray(trainPathAsArray);
            if (Arrays.equals(currentPathArray, TRAIN_PATH_TO_CHECK)) {
                FOUND_TRAIN_PATH = true;
                char[][] solutionAsStringArray = printableGridAsCharArrays();
                if (trainPath != null) {
                    solutionAsStringArray[trainPath.element >> 16][trainPath.element & 65535] = '■';
                }
                for (char[] solutionLine : solutionAsStringArray) {
                    System.out.println(solutionLine);
                }
            }
        }
    }

    /**
     * Used for debugging.
     */
    public @NotNull String[] bitSetToStringGrid(@NotNull BitSet bitSet) {
        String[] result = new String[level.height];
        for (int lineIndex = 0; lineIndex < level.height; lineIndex++) {
            String line = "";
            for (int columnIndex = 0; columnIndex < level.width; columnIndex++) {
                line += bitSet.get(lineIndex * level.width + columnIndex) ? "1" : "0";
            }
            result[lineIndex] = line;
        }
        return result;
    }

    @NotNull String[] printableGrid() {
        char[][] lines = printableGridAsCharArrays();
        String[] result = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = new String(lines[i]);
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
                byte element = level.grid[(lineIndex * level.width) + columnIndex];
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

        Coordinates currentPoint = new Coordinates(level.entry >> 16, level.entry & 65535);
        result[level.entry >> 16][level.entry & 65535] = levelParser.elementsToChars.get(MapElement.ENTRY_INDEX);
        result[exitCoordinates >> 16][exitCoordinates & 65535] = levelParser.elementsToChars.get(MapElement.EXIT_INDEX);

        List<Integer> pathAsLIst;
        if(previousTrainPath != null) {
            pathAsLIst = previousTrainPath.getAsList(level);
            if (currentStepTargetCoordinates != -1) {
                pathAsLIst.add(currentStepTargetCoordinates);
            }
        } else {
            pathAsLIst = new ArrayList<>();
        }
        int[] trainPath = Level.listToPrimitiveIntArray(pathAsLIst);
        for (int i = 0; i < trainPath.length - 1; i++) {
            int from = trainPath[i];
            int fromLine = from >> 16;
            int fromColumn = from & 65535;
            int to = trainPath[i + 1];
            int toLine = to >> 16;
            int toColumn = to & 65535;
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
        result[exitCoordinates >> 16][exitCoordinates & 65535] = levelParser.elementsToChars.get(MapElement.EXIT_INDEX);
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
