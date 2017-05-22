package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Represents a state of the level Map.
 */
final class MapState {

    /**
     * Used for debugging.
     */
    static Coordinates[] TRAIN_PATH_TO_CHECK = null;

    /**
     * Used for debugging.
     */
    static int MAX_COMMON_PATH_LENGTH = 0;

    private static boolean REGISTERED_SIGNAL = false;

    static {
        Signal.handle(new Signal("USR2"), signal -> REGISTERED_SIGNAL = true);
    }

    /**
     * Path of the train leading to the current state.
     */
    private final @Nullable CoordinatesLinkedItem previousTrainPath;

    /**
     * Current Level.
     */
    private final @NotNull Level level;

    /**
     * False = position is empty before executing the state.
     */
    private final @NotNull BitSet previousGrid;

    /**
     * Target of the current step step be ? -1 = we are exiting
     */
    private final int currentStepTargetLine;

    /**
     * Target of the current step step be ? -1 = we are exiting
     */
    private final int currentStepTargetColumn;

    /**
     * The train elements before executing the state
     */
    private final @NotNull TrainElement[] previousTrainElements;

    private final @NotNull BitSet previousGridSegment;

    /**
     * Segment : we loaded or unloaded a monster
     * While we are on a segment we avoid turning around
     */
    private boolean enteredNewSegment = false;

    /**
     *
     */
    private boolean isNearAMonsterInOrOut = false;

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
    private int[] monsterInsGrid;

    /**
     * Map showing where are the monsters outs.
     */
    private int[][] monsterOutsGrid;

    /**
     * Coordinates of the exit.
     */
    private int exitLine;

    /**
     * Coordinates of the exit.
     */
    private int exitColumn;

    MapState(
            @NotNull Level level,
            @NotNull BitSet previousGrid,
            @NotNull BitSet previousGridSegment,
            int previousNumberOfMonsters,
            int currentStepTargetLine,
            int currentStepTargetColumn,
            @NotNull TrainElement[] previousTrainElements,
            @Nullable CoordinatesLinkedItem previousTrainPath,
            int exitLine,
            int exitColumn,
            int monsterInsIndex,
            int[] monsterInsGrid,
            int monsterOutsIndex,
            int[][] monsterOutsGrid

    ) {
        this.level = level;
        this.previousGrid = previousGrid;
        this.previousGridSegment = previousGridSegment;
        this.previousTrainPath = previousTrainPath;
        this.currentStepTargetLine = currentStepTargetLine;
        this.currentStepTargetColumn = currentStepTargetColumn;
        this.previousTrainElements = previousTrainElements;
        this.newMissingNumberOfMonsters = previousNumberOfMonsters;
        this.exitLine = exitLine;
        this.exitColumn = exitColumn;

        this.monsterInsIndex = monsterInsIndex;
        this.monsterInsGrid = monsterInsGrid;
        this.monsterOutsIndex = monsterOutsIndex;
        this.monsterOutsGrid = monsterOutsGrid;
    }

    boolean processState(@NotNull LinkedList<MapState> nextStates) {

        TrainElement[] trainElements = processTrainElements();
        if (trainElements == null) {
            return false;
        }

        if ((currentStepTargetLine == exitLine) && (currentStepTargetColumn == exitColumn)) {
            boolean isTrainEmpty = true;
            for (int i = 1; isTrainEmpty && (i < level.trainSize); i++) {
                isTrainEmpty = (trainElements[i].content == TrainElementContent.NO_CONTENT);
            }

            return (newMissingNumberOfMonsters == 0) && isTrainEmpty;
        }

        // running normally
        @Nullable CoordinatesLinkedItem trainPath = createTrainPath();
        BitSet newGrid = (BitSet) previousGrid.clone();
        addAvailableDirections(newGrid, trainElements, trainPath, nextStates);
        return false;
    }

    private @NotNull MapState createMapState(
            @NotNull BitSet grid,
            @NotNull BitSet gridCurrentSegment,
            @NotNull TrainElement[] trainElements,
            @Nullable CoordinatesLinkedItem trainPath,
            int targetLine,
            int targetColumn) {
        return new MapState(
                level,
                grid,
                gridCurrentSegment,
                newMissingNumberOfMonsters,
                targetLine,
                targetColumn,
                trainElements,
                trainPath,
                exitLine,
                exitColumn,
                monsterInsIndex,
                monsterInsGrid,
                monsterOutsIndex,
                monsterOutsGrid);
    }

    private @Nullable CoordinatesLinkedItem createTrainPath() {
        if (currentStepTargetLine != -1) {
            return new CoordinatesLinkedItem(currentStepTargetLine, currentStepTargetColumn, previousTrainPath);
        } else {
            return previousTrainPath;
        }
    }

    /**
     * Process the train elements
     *
     * @return the new train elements or null if the move is bad and we should cancel it.
     */
    private @Nullable TrainElement[] processTrainElements() {
        TrainElement[] trainElements = new TrainElement[level.trainSize];
        for (int trainElementIndex = 0; trainElementIndex < level.trainSize; trainElementIndex++) {
            TrainElement trainElement = processTrainElement(trainElementIndex);
            if (trainElement != null) {
                trainElements[trainElementIndex] = trainElement;
            } else {
                return null;
            }
        }
        return trainElements;
    }

    private @Nullable TrainElement processTrainElement(int trainElementIndex) {
        TrainElement previousTrainElement = previousTrainElements[trainElementIndex];

        // Not yet entered
        if (previousTrainElement.line == -1) {
            if (
                    (trainElementIndex != 0) &&
                            (previousTrainElements[trainElementIndex - 1].line == -1)
                    ) {
                // Should still wait
                return previousTrainElement;
            } else {
                // Just enter
                return new TrainElement(
                        TrainElementContent.NO_CONTENT,
                        level.entryLine,
                        level.entryColumn,
                        false);
            }
        } else if (trainElementIndex == 0) {
            // head element

            int currentTargetLocalCoordinates = (currentStepTargetLine * level.width) + currentStepTargetColumn;
            isNearAMonsterInOrOut = (monsterInsGrid[currentTargetLocalCoordinates] != -1) ||
                    (monsterOutsGrid[currentTargetLocalCoordinates] != null);

            return new TrainElement(
                    TrainElementContent.NO_CONTENT,
                    currentStepTargetLine,
                    currentStepTargetColumn,
                    false
            );
        }

        int newTrainElementLine = previousTrainElements[trainElementIndex - 1].line;
        int newTrainElementColumn = previousTrainElements[trainElementIndex - 1].column;

        int newTrainElementLocalCoordinates =
                (newTrainElementLine * level.width) + newTrainElementColumn;

        byte newTrainElementContent = previousTrainElement.content;
        boolean newTrainElementTaintedByGreenMonster = previousTrainElement.taintedByGreenMonster;

        {
            int[] monsterOuts = monsterOutsGrid[newTrainElementLocalCoordinates];
            if (monsterOuts != null) {
                if (trainElementIndex != (level.trainSize - 1)) {
                    isNearAMonsterInOrOut = true;
                }
                if (newTrainElementContent != TrainElementContent.NO_CONTENT) {
                    if (canEmpty(monsterOuts, newTrainElementContent)) {
                        newTrainElementContent = TrainElementContent.NO_CONTENT;
                        enteredNewSegment = true;
                    }
                }
            }
        }

        {
            int monsterInCoordinates = monsterInsGrid[newTrainElementLocalCoordinates];
            if (monsterInCoordinates != -1) {
                if (trainElementIndex != (level.trainSize - 1)) {
                    isNearAMonsterInOrOut = true;
                }
                if (newTrainElementContent == TrainElementContent.NO_CONTENT) {
                    int element = level.grid[monsterInCoordinates];
                    byte possibleNewTrainElementContent = MapElement.MONSTER[element];
                    if ((!newTrainElementTaintedByGreenMonster) || (possibleNewTrainElementContent == TrainElementContent.MONSTER_GREEN)) {
                        if (possibleNewTrainElementContent == TrainElementContent.MONSTER_GREEN) {
                            newTrainElementTaintedByGreenMonster = true;
                        }
                        newTrainElementContent = possibleNewTrainElementContent;
                        monsterInsIndex -= (1 << level.monsterInsIndexes.get(monsterInCoordinates));
                        monsterInsGrid = level.monsterInsGrids[monsterInsIndex];
                        enteredNewSegment = true;
                    }
                }
            }
        }

        {
            int[] monsterOuts = monsterOutsGrid[newTrainElementLocalCoordinates];
            if (monsterOuts != null) {
                if (newTrainElementContent != TrainElementContent.NO_CONTENT) {
                    if (canEmpty(monsterOuts, newTrainElementContent)) {
                        newTrainElementContent = TrainElementContent.NO_CONTENT;
                        enteredNewSegment = true;
                    }
                }
            }
        }

        return new TrainElement(
                newTrainElementContent,
                newTrainElementLine,
                newTrainElementColumn,
                newTrainElementTaintedByGreenMonster);
    }

    /**
     * Can empty the current content ?
     *
     * @param monsterOuts         monsters out around the train element
     * @param trainElementContent the content of the train element
     * @return true if it can be emptied
     */
    private boolean canEmpty(
            @NotNull int[] monsterOuts,
            byte trainElementContent) {
        for (int monsterOutCoordinates : monsterOuts) {
            int monsterOutElement = level.grid[monsterOutCoordinates];
            if ((trainElementContent == monsterOutElement) || (monsterOutElement == MapElement.MONSTER_RED_OUT_EMPTY_INDEX)) {
                newMissingNumberOfMonsters--;
                monsterOutsIndex -= (1 << level.monsterOutsIndexes.get(monsterOutCoordinates));
                monsterOutsGrid = level.monsterOutsGrids[monsterOutsIndex];
                return true;
            }
        }
        return false;
    }

    private void addAvailableDirections(
            @NotNull BitSet grid,
            @NotNull TrainElement[] trainElements,
            @Nullable CoordinatesLinkedItem trainPath,
            @NotNull LinkedList<MapState> nextStates) {

        int currentLine = trainElements[0].line;
        int currentColumn = trainElements[0].column;
        int currentPosition = (currentLine * level.width) + currentColumn;

        grid.set(currentPosition);

        // checkTrainPathForDebug(trainPath);

        if (REGISTERED_SIGNAL) {
            REGISTERED_SIGNAL = false;
            printGrid(trainPath);
            if (TRAIN_PATH_TO_CHECK != null) {
                System.out.println(MAX_COMMON_PATH_LENGTH);
            }
        }

        BitSet newGridCurrentSegment = enteredNewSegment ?
                new BitSet(level.width * level.height) :
                ((BitSet) previousGridSegment.clone());
        newGridCurrentSegment.set(currentPosition);

        boolean isFirstLine = (currentLine == 0);
        boolean isLastLine = (currentLine == (level.height - 1));
        boolean isFirstColumn = (currentColumn == 0);
        boolean isLastColumn = (currentColumn == (level.width - 1));

        {
            // up
            int targetPositionUp = currentPosition - level.width;
            if ((!isFirstLine) &&
                    (!grid.get(targetPositionUp))) {
                if (isNearAMonsterInOrOut || enteredNewSegment ||
                        (
                                ((currentLine == 1) || (!newGridCurrentSegment.get(targetPositionUp - level.width))) && // up
                                        (isFirstColumn || (!newGridCurrentSegment.get(targetPositionUp - 1)) && // left
                                                (isLastColumn || (!newGridCurrentSegment.get(targetPositionUp + 1))) // right
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, currentLine - 1, currentColumn));
                }
            }
        }

        {
            // down
            int targetPositionDown = currentPosition + level.width;
            if ((!isLastLine) &&
                    (!grid.get(targetPositionDown))) {
                if (isNearAMonsterInOrOut || enteredNewSegment ||
                        (
                                ((currentLine == (level.height - 2)) || (!newGridCurrentSegment.get(targetPositionDown + level.width))) && // down
                                        (isFirstColumn || (!newGridCurrentSegment.get(targetPositionDown - 1)) && // left
                                                (isLastColumn || (!newGridCurrentSegment.get(targetPositionDown + 1))) // right
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, currentLine + 1, currentColumn));
                }
            }
        }

        {
            // left
            int targetPositionLeft = currentPosition - 1;
            if ((!isFirstColumn) &&
                    (!grid.get(targetPositionLeft))) {
                if (isNearAMonsterInOrOut || enteredNewSegment ||
                        (
                                (isFirstLine || (!newGridCurrentSegment.get(targetPositionLeft - level.width))) && // up
                                        (isLastLine || (!newGridCurrentSegment.get(targetPositionLeft + level.width))) && // down
                                        ((currentColumn == 1) || (!newGridCurrentSegment.get(targetPositionLeft - 1))  // left
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, currentLine, currentColumn - 1));
                }
            }
        }

        {
            // right
            int targetPositionRight = currentPosition + 1;
            if ((!isLastColumn) &&
                    (!grid.get(targetPositionRight))) {
                if (isNearAMonsterInOrOut || enteredNewSegment ||
                        (
                                (isFirstLine || (!newGridCurrentSegment.get(targetPositionRight - level.width))) && // up
                                        (isLastLine || (!newGridCurrentSegment.get(targetPositionRight + level.width))) && // down
                                        ((currentColumn == (level.width - 2)) || (!newGridCurrentSegment.get(targetPositionRight + 1))  // right
                                        ))) {
                    nextStates.add(createMapState(grid, newGridCurrentSegment, trainElements, trainPath, currentLine, currentColumn + 1));
                }
            }
        }
    }

    /**
     * Used for debugging
     */
    private void checkTrainPathForDebug(@Nullable CoordinatesLinkedItem trainPath) {
        if (TRAIN_PATH_TO_CHECK == null) {
            throw new RuntimeException("Forgot to set TRAIN_PATH_TO_CHECK, or forgot to comment call to checkTrainPathForDebug !");
        }
        List<Coordinates> trainPathAsList = (trainPath == null) ? new ArrayList<>() : trainPath.getAsList(level);
        Coordinates[] trainPathAsArray = trainPathAsList.toArray(new Coordinates[trainPathAsList.size()]);
        int maxLength = Math.min(trainPathAsArray.length, TRAIN_PATH_TO_CHECK.length);
        for (int i = 0; i < maxLength; i++) {
            if (!trainPathAsArray[i].equals(TRAIN_PATH_TO_CHECK[i])) {
                MAX_COMMON_PATH_LENGTH = Math.max(MAX_COMMON_PATH_LENGTH, i - 1);
                return;
            }
        }
        MAX_COMMON_PATH_LENGTH = Math.max(MAX_COMMON_PATH_LENGTH, maxLength);
        if (MAX_COMMON_PATH_LENGTH == 53) {
            System.out.println("Break");
        }
    }

    private void printGrid(@Nullable CoordinatesLinkedItem trainPath) {
        char[][] solutionAsStringArray = printableGridAsCharArrays();
        if (trainPath != null) {
            solutionAsStringArray[trainPath.line][trainPath.column] = '■';
        }
        for (char[] solutionLine : solutionAsStringArray) {
            System.out.println(solutionLine);
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
                int position = (lineIndex * level.width) + columnIndex;
                byte element = level.grid[position];
                Character character = levelParser.elementsToChars.get(element);
                if (character == null) {
                    throw new RuntimeException("Unknown element [" + element + "]");
                }
                if (character == MapElement.EXIT_INDEX) {
                    character = MapElement.EMPTY_INDEX;
                }
                lineChar[columnIndex] = character;


                if (TRAIN_PATH_TO_CHECK != null) {
                    if (level.monsterInsIndexes.containsKey(position)) {
                        int monsterInIndex = level.monsterInsIndexes.get(position);
                        if ((monsterInsIndex >> monsterInIndex) == 0) {
                            lineChar[columnIndex] = levelParser.elementsToInvertChar.get(element);
                        }
                    }

                    if (level.monsterOutsIndexes.containsKey(position)) {
                        int monsterOutIndex = level.monsterOutsIndexes.get(position);
                        if ((monsterOutsIndex >> monsterOutIndex) == 0) {
                            lineChar[columnIndex] = levelParser.elementsToInvertChar.get(element);
                        }
                    }
                }

            }
            result[lineIndex] = lineChar;
        }


        Coordinates currentPoint = new Coordinates(level.entryLine, level.entryColumn);
        result[level.entryLine][level.entryColumn] = levelParser.elementsToChars.get(MapElement.ENTRY_INDEX);
        result[exitLine][exitColumn] = levelParser.elementsToChars.get(MapElement.EXIT_INDEX);

        List<Coordinates> pathAsLIst;
        if (previousTrainPath != null) {
            pathAsLIst = previousTrainPath.getAsList(level);
            if (currentStepTargetLine != -1) {
                pathAsLIst.add(new Coordinates(currentStepTargetLine, currentStepTargetColumn));
            }
        } else {
            pathAsLIst = new ArrayList<>();
        }
        Coordinates[] trainPath = pathAsLIst.toArray(new Coordinates[pathAsLIst.size()]);
        for (int i = 0; i < trainPath.length - 1; i++) {
            Coordinates from = trainPath[i];
            int fromLine = from.line;
            int fromColumn = from.column;
            Coordinates to = trainPath[i + 1];
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
        result[exitLine][exitColumn] = levelParser.elementsToChars.get(MapElement.EXIT_INDEX);
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
