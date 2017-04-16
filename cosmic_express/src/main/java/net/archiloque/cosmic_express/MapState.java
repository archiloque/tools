package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

final class MapState {

    final @NotNull int[] previousTrainPath;

    final @NotNull Level level;

    private final @NotNull int[] previousGrid;

    private final int targetCoordinates;

    private final @NotNull TrainElement[] previousTrainElements;

    private int newMissingNumberOfMonsters;

    int usedExitCoordinates;

    MapState(
            @NotNull Level level,
            @NotNull int[] previousGrid,
            int previousNumberOfMonsters,
            int targetCoordinates,
            @NotNull TrainElement[] previousTrainElements,
            @NotNull int[] previousTrainPath,
            int usedExitCoordinates
    ) {
        this.level = level;
        this.previousGrid = previousGrid;
        this.previousTrainPath = previousTrainPath;

        this.targetCoordinates = targetCoordinates;
        this.previousTrainElements = previousTrainElements;
        this.newMissingNumberOfMonsters = previousNumberOfMonsters;
        this.usedExitCoordinates = usedExitCoordinates;
    }

    @Nullable int[] processState(LinkedList<MapState> nextStates) {
        int[] grid = cloneGrid();

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

    private int[] cloneGrid() {
        int[] grid = new int[level.height * level.width];
        System.arraycopy(previousGrid, 0, grid, 0, level.width * level.height);
        return grid;
    }

    private @NotNull MapState createMapState(@NotNull int[] grid, @NotNull TrainElement[] trainElements, int[] trainPath, int targetCoordinates) {
        return new MapState(
                level,
                grid,
                newMissingNumberOfMonsters,
                targetCoordinates,
                trainElements,
                trainPath,
                usedExitCoordinates);
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

    private @Nullable TrainElement[] processTrainElements(int[] grid) {
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

    private @Nullable TrainElement processTrainElement(int[] grid, int trainElementIndex) {
        TrainElement previousTrainElement = previousTrainElements[trainElementIndex];
        int previousTrainElementLine = (previousTrainElement.coordinates == -1) ? -1 : (previousTrainElement.coordinates >> 16);
        int previousTrainElementColumn = (previousTrainElement.coordinates == -1) ? -1 : (previousTrainElement.coordinates & 65535);

        // Already exited ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.EXITED) {
            return previousTrainElement;
        }

        // Should still wait ?
        if (previousTrainElement.trainElementStatus == TrainElementStatus.WAITING) {
            if ((!previousTrainElement.head) && (previousTrainElements[trainElementIndex - 1].trainElementStatus == TrainElementStatus.WAITING)) {
                return previousTrainElement;
            }
        }

        // Will exit ?
        if ((previousTrainElement.trainElementStatus == TrainElementStatus.RUNNING) &&
                (level.grid[(previousTrainElementLine * level.width) + previousTrainElementColumn] == MapElement.EXIT_INDEX)) {
            if (previousTrainElement.content != TrainElementContent.NO_CONTENT) {
                return null;
            } else {
                usedExitCoordinates = previousTrainElement.coordinates;
                return new TrainElement(
                        previousTrainElement.head,
                        TrainElementStatus.EXITED,
                        TrainElementContent.NO_CONTENT,
                        -1
                );
            }
        }

        int newTrainElementCoordinates = calculateNewCoordinates(previousTrainElement, trainElementIndex, targetCoordinates);
        int newTrainElementLine = newTrainElementCoordinates >> 16;
        int newTrainElementColumn = newTrainElementCoordinates & 65535;

        if (previousTrainElement.head) {
            grid[(newTrainElementLine * level.width) + newTrainElementColumn] = MapElement.RAIL_INDEX;
        }

        int newTrainElementContent = previousTrainElement.content;

        // No content for head
        if (!previousTrainElement.head) {
            if (newTrainElementContent != TrainElementContent.NO_CONTENT) {
                int outBoardEmptyElement = MapElement.outBoardEmptyElement(newTrainElementContent);
                @Nullable int maybeOutCoordinates = findOutboardCoordinates(
                        newTrainElementLine,
                        newTrainElementColumn,
                        grid,
                        outBoardEmptyElement);
                if (maybeOutCoordinates != -1) {
                    grid[((maybeOutCoordinates >> 16) * level.width) + (maybeOutCoordinates & 65535)] = MapElement.outBoardFilledElement(newTrainElementContent);
                    newMissingNumberOfMonsters--;
                    newTrainElementContent = TrainElementContent.NO_CONTENT;
                }
            }

            if (newTrainElementContent == TrainElementContent.NO_CONTENT) {
                @Nullable int maybeInCoordinates = findInboardCoordinates(
                        newTrainElementLine,
                        newTrainElementColumn,
                        level,
                        grid);
                if (maybeInCoordinates != -1) {
                    int maybeInCoordinatesIndex = ((maybeInCoordinates >> 16) * level.width) + (maybeInCoordinates & 65535);
                    int inFilledElement = grid[maybeInCoordinatesIndex];
                    newTrainElementContent = MapElement.MONSTER[inFilledElement];
                    grid[maybeInCoordinatesIndex] = MapElement.inBoardEmptyElement(newTrainElementContent);
                }
            }
        }

        return new TrainElement(previousTrainElement.head,
                TrainElementStatus.RUNNING,
                newTrainElementContent,
                newTrainElementCoordinates);
    }

    private int findOutboardCoordinates(int line, int column, @NotNull int[] grid, int outBoardElement) {
        if ((line > 0) && (grid[((line - 1) * level.width) + column] == outBoardElement)) {
            return ((line - 1) << 16) + column;
        } else if ((line < (level.height - 1)) && (grid[((line + 1) * level.width) + column] == outBoardElement)) {
            return ((line + 1) << 16) + column;
        } else if ((column > 0) && (grid[(line * level.width) + column - 1] == outBoardElement)) {
            return (line << 16) + column - 1;
        } else if ((column < (level.width - 1)) && (grid[(line * level.width) + column + 1] == outBoardElement)) {
            return (line << 16) + column + 1;
        } else {
            return -1;
        }
    }

    private int findInboardCoordinates(int line, int column, @NotNull Level level, @NotNull int[] grid) {
        if ((line > 0) && MapElement.MONSTER_IN_FILLED[grid[((line - 1) * level.width) + column]]) {
            return ((line - 1) << 16) + column;
        } else if ((line < (level.height - 1)) && MapElement.MONSTER_IN_FILLED[grid[((line + 1) * level.width) + column]]) {
            return ((line + 1) << 16) + column;
        } else if ((column > 0) && MapElement.MONSTER_IN_FILLED[grid[(line * level.width) + column - 1]]) {
            return (line << 16) + column - 1;
        } else if ((column < (level.width - 1)) && MapElement.MONSTER_IN_FILLED[grid[(line * level.width) + column + 1]]) {
            return (line << 16) + column + 1;
        } else {
            return -1;
        }
    }


    private int calculateNewCoordinates(@NotNull TrainElement trainElement, int trainElementIndex, int targetPosition) {
        if (trainElement.trainElementStatus == TrainElementStatus.WAITING) {
            // Entering the board
            return level.entry;
        } else if (trainElement.head) {
            return targetPosition;
        } else {
            TrainElement previousTrainElement = previousTrainElements[trainElementIndex - 1];
            return previousTrainElement.coordinates;
        }
    }

    private void addAvailableDirections(
            @NotNull int[] grid, @NotNull TrainElement[] trainElements, @NotNull int[] trainPath, @NotNull LinkedList<MapState> nextStates) {
        int currentLine = trainElements[0].coordinates >> 16;
        int currentColumn = trainElements[0].coordinates & 65535;
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
