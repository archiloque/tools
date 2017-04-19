package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;

final class MapState {

    final @NotNull int[] previousTrainPath;

    final @NotNull Level level;

    private final @NotNull byte[] previousGrid;

    private final int targetCoordinates;

    private final @NotNull TrainElement[] previousTrainElements;

    private int newMissingNumberOfMonsters;

    private int monsterInsIndex;

    private int monsterOutsIndex;

    private int[][] monsterInsGrid;

    private int[][] monsterOutsGrid;

    int exitCoordinates;

    MapState(
            @NotNull Level level,
            @NotNull byte[] previousGrid,
            int previousNumberOfMonsters,
            int targetCoordinates,
            @NotNull TrainElement[] previousTrainElements,
            @NotNull int[] previousTrainPath,
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
        this.exitCoordinates =exitCoordinates;
    }

    @Nullable byte[] processState(LinkedList<MapState> nextStates) {
        byte[] grid = cloneGrid();

        TrainElement[] trainElements = processTrainElements(grid);
        if (trainElements == null) {
            return null;
        }

        // all exited ?
        if (trainElements[level.trainSize - 1].trainElementStatus == TrainElementStatus.EXITED) {
            return (newMissingNumberOfMonsters == 0) ? grid : null;
        } else if (trainElements[0].trainElementStatus == TrainElementStatus.EXITED) {
            nextStates.addFirst(createMapState(grid, trainElements, previousTrainPath, -1));
            return null;
        } else {
            @NotNull int[] trainPath = createTrainPath();
            addAvailableDirections(grid, trainElements, trainPath, nextStates);
            return null;
        }
    }

    private byte[] cloneGrid() {
        byte[] grid = new byte[level.height * level.width];
        System.arraycopy(previousGrid, 0, grid, 0, level.width * level.height);
        return grid;
    }

    private @NotNull MapState createMapState(@NotNull byte[] grid, @NotNull TrainElement[] trainElements, int[] trainPath, int targetCoordinates) {
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

    private @NotNull int[] createTrainPath() {
        if (targetCoordinates != -1) {
            int[] trainPath = new int[previousTrainPath.length + 1];
            System.arraycopy(previousTrainPath, 0, trainPath, 0, previousTrainPath.length);
            trainPath[previousTrainPath.length] = targetCoordinates;
            return trainPath;
        } else {
            return previousTrainPath;
        }
    }

    private @Nullable TrainElement[] processTrainElements(byte[] grid) {
        TrainElement[] trainElements = new TrainElement[level.trainSize];
        for (int trainElementIndex = 0; trainElementIndex < level.trainSize; trainElementIndex++) {
            TrainElement trainElement = processTrainElement(grid, trainElementIndex);
            if (trainElement != null) {
                trainElements[trainElementIndex] = trainElement;
            } else {
                return null;
            }
        }
        return trainElements;
    }

    private @Nullable TrainElement processTrainElement(byte[] grid, int trainElementIndex) {
        TrainElement previousTrainElement = previousTrainElements[trainElementIndex];

        // Already exited ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.EXITED) {
            return previousTrainElement;
        }

        // Should still wait ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.WAITING) {
            if ((trainElementIndex != 0) && (previousTrainElements[trainElementIndex - 1].trainElementStatus == TrainElementStatus.WAITING)) {
                return previousTrainElement;
            }
        }

        // Will exit ?
        if ((previousTrainElement.trainElementStatus == TrainElementStatus.RUNNING) &&
                (previousTrainElement.coordinates == exitCoordinates)) {
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

        int newTrainElementCoordinates = calculateNewCoordinates(previousTrainElement, trainElementIndex, targetCoordinates);
        int newTrainElementLocalCoordinates = ((newTrainElementCoordinates >> 16) * level.width) + (newTrainElementCoordinates & 65535);

        if (trainElementIndex == 0) {
            grid[newTrainElementLocalCoordinates] = MapElement.RAIL_INDEX;
        }

        byte newTrainElementContent = previousTrainElement.content;

        // No content for head
        if (trainElementIndex != 0) {
            if (newTrainElementContent != TrainElementContent.NO_CONTENT) {
                if(canEmpty(grid, monsterOutsGrid[newTrainElementLocalCoordinates], newTrainElementContent)) {
                    newTrainElementContent = TrainElementContent.NO_CONTENT;
                }
            }

            if (newTrainElementContent == TrainElementContent.NO_CONTENT) {
                int[] monsterIns = monsterInsGrid[newTrainElementLocalCoordinates];
                if((monsterIns != null) && (monsterIns.length == 1)) {
                    int monsterInCoordinates = monsterIns[0];
                    int element = grid[monsterInCoordinates];
                    newTrainElementContent = MapElement.MONSTER[element];
                    grid[monsterInCoordinates] = MapElement.inBoardEmptyElement(newTrainElementContent);
                    monsterInsIndex -= 1 << Arrays.binarySearch(level.monsterIns, monsterInCoordinates);
                    monsterInsGrid = level.monsterInsGrids[monsterInsIndex];
                }
            }
        }

        return new TrainElement(TrainElementStatus.RUNNING,
                newTrainElementContent,
                newTrainElementCoordinates);
    }

    private boolean canEmpty(byte[] grid, int[] monsterOuts, byte newTrainElementContent) {
        if(monsterOuts != null) {
            int outBoardEmptyElement = MapElement.outBoardEmptyElement(newTrainElementContent);
            for (int monsterOutCoordinates : monsterOuts) {
                int monsterOutElement = grid[monsterOutCoordinates];
                if (outBoardEmptyElement == monsterOutElement) {
                    grid[monsterOutCoordinates] = MapElement.outBoardFilledElement(newTrainElementContent);
                    newMissingNumberOfMonsters--;
                    monsterOutsIndex -= 1 << Arrays.binarySearch(level.monsterOuts, monsterOutCoordinates);
                    monsterOutsGrid = level.monsterOutsGrids[monsterOutsIndex];
                    return true;
                }
            }
        }
        return false;
    }


    private int calculateNewCoordinates(@NotNull TrainElement trainElement, int trainElementIndex, int targetPosition) {
        if (trainElement.trainElementStatus == TrainElementStatus.WAITING) {
            // Entering the board
            return level.entry;
        } else if (trainElementIndex == 0) {
            return targetPosition;
        } else {
            TrainElement previousTrainElement = previousTrainElements[trainElementIndex - 1];
            return previousTrainElement.coordinates;
        }
    }

    private void addAvailableDirections(
            @NotNull byte[] grid, @NotNull TrainElement[] trainElements, @NotNull int[] trainPath, @NotNull LinkedList<MapState> nextStates) {
        TrainElement trainHead = trainElements[0];
        int currentLine = trainHead.coordinates >> 16;
        int currentColumn = trainHead.coordinates & 65535;
        if ((currentLine > 0) && MapElement.CROSSABLE[grid[((currentLine - 1) * level.width) + currentColumn]]) {
            nextStates.addFirst(createMapState(grid, trainElements, trainPath, ((currentLine - 1) << 16) + currentColumn));
        }
        if ((currentLine < (level.height - 1)) && MapElement.CROSSABLE[grid[((currentLine + 1) * level.width) + currentColumn]]) {
            nextStates.addFirst(createMapState(grid, trainElements, trainPath, ((currentLine + 1) << 16) + currentColumn));
        }
        if ((currentColumn > 0) && MapElement.CROSSABLE[grid[(currentLine * level.width) + currentColumn - 1]]) {
            nextStates.addFirst(createMapState(grid, trainElements, trainPath, (currentLine << 16) + currentColumn - 1));
        }
        if ((currentColumn < (level.width - 1)) && MapElement.CROSSABLE[grid[(currentLine * level.width) + currentColumn + 1]]) {
            nextStates.addFirst(createMapState(grid, trainElements, trainPath, (currentLine << 16) + currentColumn + 1));
        }
    }


}
