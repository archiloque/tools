package net.archiloque.cosmic_express;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents the structure of a level.
 */
class Level {

    final int trainSize;

    final int height;

    final int width;

    /**
     * Contains MapElements.
     */
    final byte[] grid;

    int[] monsterIns;

    int[] monsterOuts;

    int[][][] monsterInsGrids;

    int[][][] monsterOutsGrids;

    int entry;

    Level(int trainSize, int height, int width) {
        this.trainSize = trainSize;
        this.height = height;
        this.width = width;

        grid = new byte[height * width];
        Arrays.fill(grid, MapElement.EMPTY_INDEX);
    }

    void setElement(byte mapElement, Coordinates coordinates) {
        grid[(coordinates.line * width) + coordinates.column] = mapElement;
        if (mapElement == MapElement.ENTRY_INDEX) {
            entry = (coordinates.line << 16) + coordinates.column;
        }
    }

    List<MapState> createMapState() {
        int numberOfMonsters = 0;
        List<Coordinates> exitCoordinates = new ArrayList<>();

        List<Integer> monstersInsList = new ArrayList<>();
        List<Integer> monstersOutsList = new ArrayList<>();

        for (int lineIndex = 0; lineIndex < height; lineIndex++) {
            for (int columnIndex = 0; columnIndex < width; columnIndex++) {
                int mapElement = grid[(lineIndex * width) + columnIndex];
                if (MapElement.MONSTER_IN_FILLED[mapElement]) {
                    numberOfMonsters += 1;
                    monstersInsList.add(lineIndex * width + columnIndex);
                } else if (MapElement.MONSTER_OUT_EMPTY[mapElement]) {
                    monstersOutsList.add(lineIndex * width + columnIndex);
                } else if (mapElement == MapElement.EXIT_INDEX) {
                    exitCoordinates.add(new Coordinates(lineIndex, columnIndex));
                }
            }
        }

        monsterIns = toPrimitiveInt(monstersInsList);
        int monstersInsPossibilities = 1 << monsterIns.length;
        monsterInsGrids = new int[monstersInsPossibilities][][];
        for (int i = 0; i < monstersInsPossibilities; i++) {
            monsterInsGrids[i] = calculateMonsterGrid(i, monsterIns);
        }

        monsterOuts = toPrimitiveInt(monstersOutsList);
        int monstersOutsPossibilities = 1 << monsterOuts.length;
        monsterOutsGrids = new int[monstersOutsPossibilities][][];
        for (int i = 0; i < monstersOutsPossibilities; i++) {
            monsterOutsGrids[i] = calculateMonsterGrid(i, monsterOuts);
        }

        TrainElement[] trainElements = new TrainElement[trainSize];
        for (int trainElementIndex = 0; trainElementIndex < trainSize; trainElementIndex++) {
            trainElements[trainElementIndex] = new TrainElement(
                    TrainElementStatus.WAITING,
                    TrainElementContent.NO_CONTENT,
                    -1
            );

        }

        List<MapState> result = new ArrayList<>(exitCoordinates.size());
        for (int exitIndex = 0; exitIndex < exitCoordinates.size(); exitIndex++) {
            Coordinates currentExit = exitCoordinates.get(exitIndex);
            byte[] gridForExit = new byte[height * width];
            System.arraycopy(grid, 0, gridForExit, 0, width * height);
            for (int otherExitIndex = 0; otherExitIndex < exitCoordinates.size(); otherExitIndex++) {
                if (otherExitIndex != exitIndex) {
                    Coordinates exitCoordinate = exitCoordinates.get(otherExitIndex);
                    gridForExit[(exitCoordinate.line * width) + exitCoordinate.column] = MapElement.OBSTACLE_INDEX;
                }
            }
            result.add(new MapState(this,
                            gridForExit,
                            numberOfMonsters,
                            -1,
                            trainElements,
                            new int[0],
                            monstersInsPossibilities - 1,
                            monsterInsGrids[monstersInsPossibilities - 1],
                            monstersOutsPossibilities - 1,
                            monsterOutsGrids[monstersOutsPossibilities - 1],
                            (currentExit.line << 16) + currentExit.column
                    )
            );

        }
        return result;
    }

    private int[] toPrimitiveInt(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private void addPosition(List<Integer>[] resultWithLists, int line, int column, int currentPosition) {
        int targetPosition = (line * width) + column;
        List<Integer> currentList = resultWithLists[targetPosition];
        if (currentList == null) {
            currentList = new ArrayList<>();
            resultWithLists[targetPosition] = currentList;
        }
        currentList.add(currentPosition);

    }

    private int[][] calculateMonsterGrid(int index, int[] positions) {
        List<Integer>[] resultWithLists = new List[width * height];
        for (int i = 0; i < positions.length; i++) {
            if ((index & (1 << i)) != 0) {
                int currentPosition = positions[i];
                int line = currentPosition / width;
                int column = currentPosition % width;
                // above
                if (line > 0) {
                    addPosition(resultWithLists, line - 1, column, currentPosition);
                }
                // under
                if (line < (height - 1)) {
                    addPosition(resultWithLists, line + 1, column, currentPosition);
                }
                // left
                if (column > 0) {
                    addPosition(resultWithLists, line, column - 1, currentPosition);
                }
                // right
                if (column < (width - 1)) {
                    addPosition(resultWithLists, line, column + 1, currentPosition);
                }

            }
        }
        int[][] realResult = new int[width * height][];
        for (int i = 0; i < realResult.length; i++) {
            List<Integer> currentGrid = resultWithLists[i];
            if (currentGrid != null) {
                int[] currentGridPrimitive = toPrimitiveInt(currentGrid);
                Arrays.sort(currentGridPrimitive);
                realResult[i] = currentGridPrimitive;
            }

        }
        return realResult;
    }


}