package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

final class MapState {

    /**
     * Path of the train leading to the current state.
     */
    final @Nullable LinkedIntElement previousTrainPath;

    /**
     * Current Level.
     */
    private final @NotNull Level level;

    /**
     * Grid empty squares before executing the state.
     */
    private final @NotNull boolean[] previousGrid;

    /**
     * Where will the next step be ? -1 = we are exiting
     */
    private final int targetCoordinates;

    /**
     * The train elements before executing the state
     */
    private final @NotNull TrainElement[] previousTrainElements;

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
            @NotNull boolean[] previousGrid,
            int previousNumberOfMonsters,
            int targetCoordinates,
            @NotNull TrainElement[] previousTrainElements,
            @Nullable LinkedIntElement previousTrainPath,
            int monsterInsIndex,
            int[][] monsterInsGrid,
            int monsterOutsIndex,
            int[][] monsterOutsGrid,
            int exitCoordinates

    ) {
        this.level = level;
        this.previousGrid = previousGrid;
        this.previousTrainPath = previousTrainPath;

        this.targetCoordinates = targetCoordinates;
        this.previousTrainElements = previousTrainElements;
        this.newMissingNumberOfMonsters = previousNumberOfMonsters;
        this.monsterInsIndex = monsterInsIndex;
        this.monsterInsGrid = monsterInsGrid;
        this.monsterOutsIndex = monsterOutsIndex;
        this.monsterOutsGrid = monsterOutsGrid;
        this.exitCoordinates = exitCoordinates;
    }

    @Nullable boolean[] processState(@NotNull LinkedList<MapState> nextStates) {
        boolean[] newGrid = cloneGrid();

        TrainElement[] trainElements = processTrainElements(newGrid);
        if (trainElements == null) {
            return null;
        }

        // all exited ?
        if (trainElements[level.trainSize - 1].trainElementStatus == TrainElementStatus.EXITED) {
            return (newMissingNumberOfMonsters == 0) ? newGrid : null;
        } else if (trainElements[0].trainElementStatus == TrainElementStatus.EXITED) {
            // we are exiting : we go to the exit
            nextStates.add(createMapState(newGrid, trainElements, previousTrainPath, -1));
            return null;
        } else {
            // running normally
            @Nullable LinkedIntElement trainPath = createTrainPath();
            addAvailableDirections(newGrid, trainElements, trainPath, nextStates);
            return null;
        }
    }

    private @NotNull  boolean[] cloneGrid() {
        boolean[] grid = new boolean[level.height * level.width];
        System.arraycopy(previousGrid, 0, grid, 0, level.width * level.height);
        return grid;
    }

    private @NotNull MapState createMapState(
            @NotNull boolean[] grid,
            @NotNull TrainElement[] trainElements,
            @Nullable LinkedIntElement trainPath,
            int targetCoordinates) {
        return new MapState(
                level,
                grid,
                newMissingNumberOfMonsters,
                targetCoordinates,
                trainElements,
                trainPath,
                monsterInsIndex,
                monsterInsGrid,
                monsterOutsIndex,
                monsterOutsGrid,
                exitCoordinates);
    }

    private @Nullable LinkedIntElement createTrainPath() {
        if (targetCoordinates != -1) {
            return new LinkedIntElement(targetCoordinates, previousTrainPath);
        } else {
            return previousTrainPath;
        }
    }

    /**
     * Process the train elements
     * @param newGrid the current grid.
     * @return the new train elements or null if the move is bad and we should cancel it.
     */
    private @Nullable TrainElement[] processTrainElements(@NotNull boolean[] newGrid) {
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

    private @Nullable TrainElement processTrainElement(@NotNull boolean[] newGrid, int trainElementIndex) {
        TrainElement previousTrainElement = previousTrainElements[trainElementIndex];

        // Already exited ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.EXITED) {
            return previousTrainElement;
        }

        // Should still wait ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.WAITING) {
            if (
                    (trainElementIndex != 0) &&
                            (previousTrainElements[trainElementIndex - 1].trainElementStatus == TrainElementStatus.WAITING)
                    ) {
                return previousTrainElement;
            }
        }

        // Will exit ?
        if (previousTrainElement.coordinates == exitCoordinates) {
            if (previousTrainElement.content != TrainElementContent.NO_CONTENT) {
                return null;
            } else {
                return new TrainElement(
                        TrainElementStatus.EXITED,
                        TrainElementContent.NO_CONTENT,
                        -1
                );
            }
        }

        int newTrainElementCoordinates =
                calculateNewCoordinates(previousTrainElement, trainElementIndex, targetCoordinates);
        int newTrainElementLocalCoordinates =
                ((newTrainElementCoordinates >> 16) * level.width) + (newTrainElementCoordinates & 65535);

        if (trainElementIndex == 0) {
            newGrid[newTrainElementLocalCoordinates] = false;
        }

        byte newTrainElementContent = previousTrainElement.content;

        // No content for head
        if (trainElementIndex != 0) {
            if (newTrainElementContent != TrainElementContent.NO_CONTENT) {
                if (canEmpty(newGrid, monsterOutsGrid[newTrainElementLocalCoordinates], newTrainElementContent)) {
                    newTrainElementContent = TrainElementContent.NO_CONTENT;
                }
            }

            if (newTrainElementContent == TrainElementContent.NO_CONTENT) {
                int[] monsterIns = monsterInsGrid[newTrainElementLocalCoordinates];
                if ((monsterIns != null) && (monsterIns.length == 1)) {
                    int monsterInCoordinates = monsterIns[0];
                    int element = level.grid[monsterInCoordinates];
                    newTrainElementContent = MapElement.MONSTER[element];
                    newGrid[monsterInCoordinates] = false;
                    monsterInsIndex -= 1 << Arrays.binarySearch(level.monsterIns, monsterInCoordinates);
                    monsterInsGrid = level.monsterInsGrids[monsterInsIndex];
                }
            }
        }

        return new TrainElement(TrainElementStatus.RUNNING,
                newTrainElementContent,
                newTrainElementCoordinates);
    }

    private boolean canEmpty(
            @NotNull boolean[] grid,
            @Nullable int[] monsterOuts,
            byte newTrainElementContent) {
        if (monsterOuts != null) {
            int outBoardEmptyElement = MapElement.outBoardEmptyElement(newTrainElementContent);
            for (int monsterOutCoordinates : monsterOuts) {
                int monsterOutElement = level.grid[monsterOutCoordinates];
                if (outBoardEmptyElement == monsterOutElement) {
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
            @NotNull boolean[] grid,
            @NotNull TrainElement[] trainElements,
            @Nullable LinkedIntElement trainPath,
            @NotNull LinkedList<MapState> nextStates) {
        TrainElement trainHead = trainElements[0];
        int currentLine = trainHead.coordinates >> 16;
        int currentColumn = trainHead.coordinates & 65535;
        if ((currentLine > 0) && grid[((currentLine - 1) * level.width) + currentColumn]) {
            nextStates.add(createMapState(grid, trainElements, trainPath, ((currentLine - 1) << 16) + currentColumn));
        }
        if ((currentLine < (level.height - 1)) && grid[((currentLine + 1) * level.width) + currentColumn]) {
            nextStates.add(createMapState(grid, trainElements, trainPath, ((currentLine + 1) << 16) + currentColumn));
        }
        if ((currentColumn > 0) && grid[(currentLine * level.width) + currentColumn - 1]) {
            nextStates.add(createMapState(grid, trainElements, trainPath, (currentLine << 16) + currentColumn - 1));
        }
        if ((currentColumn < (level.width - 1)) && grid[(currentLine * level.width) + currentColumn + 1]) {
            nextStates.add(createMapState(grid, trainElements, trainPath, (currentLine << 16) + currentColumn + 1));
        }
    }


}
