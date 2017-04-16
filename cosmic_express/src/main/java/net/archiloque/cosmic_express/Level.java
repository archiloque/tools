package net.archiloque.cosmic_express;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Level {

    final int trainSize;

    final int height;

    final int width;

    final int[] grid;

    int entry;

    Level(int trainSize, int height, int width) {
        this.trainSize = trainSize;
        this.height = height;
        this.width = width;

        grid = new int[height * width];
        Arrays.fill(grid, MapElement.EMPTY_INDEX);
    }

    void setElement(int mapElement, Coordinates coordinates) {
        grid[(coordinates.line * width) + coordinates.column] = mapElement;
        if(mapElement == MapElement.ENTRY_INDEX) {
            entry = (coordinates.line << 16) + coordinates.column;;
        }
    }

    List<MapState> createMapState(){
        int numberOfMonsters = 0;
        List<Coordinates> exitCoordinates = new ArrayList<>();
        for (int lineIndex = 0; lineIndex < height; lineIndex++) {
            for (int columnIndex = 0; columnIndex < width; columnIndex++) {
                int mapElement = grid[(lineIndex * width) + columnIndex];
                if(MapElement.MONSTER_IN_FILLED[mapElement]){
                    numberOfMonsters += 1;
                }
                if(mapElement == MapElement.EXIT_INDEX) {
                    exitCoordinates.add(new Coordinates(lineIndex, columnIndex));
                }
            }
        }
        TrainElement[] trainElements = new TrainElement[trainSize];
        for (int trainElementIndex = 0; trainElementIndex < trainSize; trainElementIndex++) {
            trainElements[trainElementIndex] = new TrainElement(
                    trainElementIndex == 0,
                    TrainElementStatus.WAITING,
                    TrainElementContent.NO_CONTENT,
                    -1
            );

        }

        List<MapState> result = new ArrayList<>(exitCoordinates.size());
        for(int exitIndex = 0; exitIndex < exitCoordinates.size(); exitIndex++) {
            int[] gridForExit = new int[height * width];
            System.arraycopy(grid , 0, gridForExit, 0, width * height);
            for(int otherExitIndex = 0; otherExitIndex < exitCoordinates.size(); otherExitIndex++) {
                if(otherExitIndex != exitIndex) {
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
                    -1)
            );

        }
        return result;
    }


}
