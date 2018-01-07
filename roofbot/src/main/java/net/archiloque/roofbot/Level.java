package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Level {

    final int height;

    final int width;

    /**
     * Contains MapElements.
     */
    final byte[] gridElements;

    /**
     * Contains MapElements.
     */
    final int[] gridStrengths;

    int entryLine;

    int entryColumn;

    int exitLine;

    int exitColumn;

    private int numberOfElements = 0;

    final List<Integer>[] basementTiles;

    final List<Coordinates> teleportersTiles = new ArrayList<>();

    Level(int height, int width) {
        this.height = height;
        this.width = width;
        gridElements = new byte[height * width];
        Arrays.fill(gridElements, MapElement.EMPTY_INDEX);
        gridStrengths = new int[height * width];
        Arrays.fill(gridStrengths, 0);
        basementTiles = new ArrayList[MapElement.NUMBER_OF_ELEMENTS];
    }

    void setElement(byte mapElement, int line, int column) {
        gridElements[(line * width) + column] = mapElement;
        if (mapElement == MapElement.ENTRY_INDEX) {
            entryLine = line;
            entryColumn = column;
        } else if (mapElement == MapElement.EXIT_INDEX) {
            exitLine = line;
            exitColumn = column;
        } else if(mapElement == MapElement.TELEPORTER_INDEX) {
            teleportersTiles.add(new Coordinates(line, column));
        }
        if (MapElement.HOLES.contains(mapElement)) {
            numberOfElements++;
        }
    }

    void setStrength(int strength, int line, int column) {
        gridStrengths[(line * width) + column] = strength;
    }

    void setBasement(byte basementType, int line, int column) {
        List<Integer> currentBasementTiles = basementTiles[basementType];
        if (currentBasementTiles == null) {
            currentBasementTiles = new ArrayList<>();
            basementTiles[basementType] = currentBasementTiles;
        }
        currentBasementTiles.add((line * width) + column);
    }


    @NotNull MapState createMapState() {
        int entryPosition = entryLine * width + entryColumn;
        int[] initialGridStrengths = gridStrengths.clone();
        initialGridStrengths[entryPosition]--;

        return new MapState(
                this,
                entryPosition,
                entryLine,
                entryColumn,
                MapElement.EMPTY_INDEX,
                initialGridStrengths,
                numberOfElements,
                new CoordinatesLinkedItem(entryLine, entryColumn, null)
        );
    }


}
