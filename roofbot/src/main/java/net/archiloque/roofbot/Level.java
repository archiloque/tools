package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    final List<Coordinates> teleporters1Tiles = new ArrayList<>();
    final List<Coordinates> teleporters2Tiles = new ArrayList<>();

    private static final byte[] SHOULD_HAVE_ONE = new byte[]{
            MapElement.ENTRY_INDEX,
            MapElement.EXIT_INDEX,

    };

    private static final byte[] SHOULD_HAVE_ZERO_OR_TWO = new byte[]{
            MapElement.TELEPORTER_1_INDEX,
            MapElement.TELEPORTER_2_INDEX,
    };

    private static final byte[][] SHOULD_HAVE_SAME_NUMBER = new byte[][]{
            new byte[]{MapElement.GREEN_OBJECT_INDEX, MapElement.GREEN_HOLE_INDEX},
            new byte[]{MapElement.BLUE_OBJECT_INDEX, MapElement.BLUE_HOLE_INDEX},
            new byte[]{MapElement.YELLOW_OBJECT_INDEX, MapElement.YELLOW_HOLE_INDEX},
            new byte[]{MapElement.RED_OBJECT_INDEX, MapElement.RED_HOLE_INDEX}
    };

    Level(int height, int width) {
        this.height = height;
        this.width = width;
        gridElements = new byte[height * width];
        Arrays.fill(gridElements, MapElement.EMPTY_INDEX);
        gridStrengths = new int[height * width];
        Arrays.fill(gridStrengths, 0);
        basementTiles = new ArrayList[MapElement.NUMBER_OF_ELEMENTS];
    }

    void validate(LevelParser levelParser) {
        Map<Byte, Integer> frequencies = new HashMap<>(MapElement.NUMBER_OF_ELEMENTS);
        for (byte element : gridElements) {
            if (frequencies.containsKey(element)) {
                frequencies.put(element, frequencies.get(element) + 1);
            } else {
                frequencies.put(element, 1);
            }
        }
        for (byte b : SHOULD_HAVE_ONE) {
            shouldHaveOne(frequencies, b, levelParser);
        }
        for (byte b : SHOULD_HAVE_ZERO_OR_TWO) {
            shouldHaveZeroOrTwo(frequencies, b, levelParser);
        }
        for (byte[] b : SHOULD_HAVE_SAME_NUMBER) {
            shouldHaveSameNumber(frequencies, b[0], b[1], levelParser);
        }

    }

    private void shouldHaveOne(Map<Byte, Integer> frequencies, byte element, LevelParser levelParser) {
        if (!frequencies.containsKey(element)) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element) + "] should be present 1 time but is 0");
        }
        Integer number = frequencies.get(element);
        if (number != 1) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element) + "] should be present 1 time but is " + number);
        }
    }

    private void shouldHaveZeroOrTwo(Map<Byte, Integer> frequencies, byte element, LevelParser levelParser) {
        if (!frequencies.containsKey(element)) {
            return;
        }
        Integer number = frequencies.get(element);
        if (number != 2) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element) + "] should be present 1 time but is " + number);
        }
    }

    private void shouldHaveSameNumber(Map<Byte, Integer> frequencies, byte element1, byte element2, LevelParser levelParser) {
        if (!frequencies.containsKey(element1)) {
            if (frequencies.containsKey(element2)) {
                throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element1) + "] and [" + levelParser.elementsToChars.get(element2) + "] have not the same frequency");
            }
        }
        if (!frequencies.containsKey(element2)) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element1) + "] and [" + levelParser.elementsToChars.get(element2) + "] have not the same frequency");
        }

        Integer number1 = frequencies.get(element1);
        Integer number2 = frequencies.get(element2);
        if (!number1.equals(number2)) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element1) + "] and [" + levelParser.elementsToChars.get(element2) + "] have not the same frequency");
        }
    }

    void setElement(byte mapElement, int line, int column) {
        gridElements[(line * width) + column] = mapElement;
        if (mapElement == MapElement.ENTRY_INDEX) {
            entryLine = line;
            entryColumn = column;
        } else if (mapElement == MapElement.EXIT_INDEX) {
            exitLine = line;
            exitColumn = column;
        } else if (mapElement == MapElement.TELEPORTER_1_INDEX) {
            teleporters1Tiles.add(new Coordinates(line, column));
        } else if (mapElement == MapElement.TELEPORTER_2_INDEX) {
            teleporters2Tiles.add(new Coordinates(line, column));
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
