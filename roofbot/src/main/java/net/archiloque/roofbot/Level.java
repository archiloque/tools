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
    final byte[] gridStrengths;

    byte entryLine;

    byte entryColumn;

    int exitLine;

    int exitColumn;

    private byte numberOfElements = 0;

    /**
     * List of tiles in the basement indexed by the type
     */
    final @NotNull List<Integer>[] basementTiles;

    private final MapState[] mapStates;

    private int mapStateNumber = 0;

    private final @NotNull List<Coordinates> teleporters1Tiles = new ArrayList<>();
    private final @NotNull List<Coordinates> teleporters2Tiles = new ArrayList<>();
    final @NotNull Map<Integer, Coordinates> teleporters = new HashMap<>();
    final @NotNull boolean[] canGoUp;
    final @NotNull boolean[] canGoDown;
    final @NotNull boolean[] canGoLeft;
    final @NotNull boolean[] canGoRight;

    private static final @NotNull byte[] SHOULD_HAVE_ONE = new byte[]{
            MapElement.ENTRY_INDEX,
            MapElement.EXIT_INDEX,
    };

    private static final @NotNull byte[] SHOULD_HAVE_ZERO_OR_TWO = new byte[]{
            MapElement.TELEPORTER_1_INDEX,
            MapElement.TELEPORTER_2_INDEX,
    };

    private static final @NotNull byte[][] SHOULD_HAVE_SAME_NUMBER = new byte[][]{
            new byte[]{MapElement.GREEN_OBJECT_INDEX, MapElement.GREEN_HOLE_INDEX},
            new byte[]{MapElement.BLUE_OBJECT_INDEX, MapElement.BLUE_HOLE_INDEX},
            new byte[]{MapElement.YELLOW_OBJECT_INDEX, MapElement.YELLOW_HOLE_INDEX},
            new byte[]{MapElement.RED_OBJECT_INDEX, MapElement.RED_HOLE_INDEX},
    };

    Level(int height, int width) {
        this.height = height;
        this.width = width;
        int numberOfTiles = height * width;
        gridElements = new byte[numberOfTiles];
        Arrays.fill(gridElements, MapElement.EMPTY_INDEX);
        gridStrengths = new byte[numberOfTiles];
        Arrays.fill(gridStrengths, (byte) 0);
        basementTiles = new ArrayList[MapElement.NUMBER_OF_ELEMENTS];
        mapStates = new MapState[numberOfTiles];
        canGoUp = new boolean[numberOfTiles];
        canGoDown = new boolean[numberOfTiles];
        canGoLeft = new boolean[numberOfTiles];
        canGoRight = new boolean[numberOfTiles];
        Arrays.fill(canGoUp, true);
        Arrays.fill(canGoDown, true);
        Arrays.fill(canGoLeft, true);
        Arrays.fill(canGoRight, true);
        for (int column = 0; column < width; column++) {
            canGoUp[column] = false;
            canGoDown[(height - 1) * width + column] = false;
        }
        for (int line = 0; line < height; line++) {
            canGoLeft[(line * width)] = false;
            canGoRight[(line * width) + (width - 1)] = false;
        }
    }

    void validate(@NotNull LevelParser levelParser) {
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
        processTeleportersTiles(teleporters1Tiles);
        processTeleportersTiles(teleporters2Tiles);
    }

    private void processTeleportersTiles(@NotNull List<Coordinates> teleportersTilesList) {
        if (!teleportersTilesList.isEmpty()) {
            Coordinates from = teleportersTilesList.get(0);
            Coordinates to = teleportersTilesList.get(1);
            Integer fromInteger = (from.line * width) + from.column;
            Integer toInteger = (to.line * width) + to.column;
            teleporters.put(fromInteger, to);
            teleporters.put(toInteger, from);
        }
    }

    private void shouldHaveOne(@NotNull Map<Byte, Integer> frequencies, byte element, @NotNull LevelParser levelParser) {
        if (!frequencies.containsKey(element)) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element) + "] should be present 1 time but is 0");
        }
        Integer number = frequencies.get(element);
        if (number != 1) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element) + "] should be present 1 time but is " + number);
        }
    }

    private void shouldHaveZeroOrTwo(@NotNull Map<Byte, Integer> frequencies, byte element, @NotNull LevelParser levelParser) {
        if (!frequencies.containsKey(element)) {
            return;
        }
        Integer number = frequencies.get(element);
        if (number != 2) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element) + "] should be present 1 time but is " + number);
        }
    }

    private void shouldHaveSameNumber(@NotNull Map<Byte, Integer> frequencies, byte element1, byte element2, @NotNull LevelParser levelParser) {
        if (!frequencies.containsKey(element1)) {
            if (frequencies.containsKey(element2)) {
                throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element1) + "] and [" + levelParser.elementsToChars.get(element2) + "] have not the same frequency");
            }
            return;
        } else if (!frequencies.containsKey(element2)) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element1) + "] and [" + levelParser.elementsToChars.get(element2) + "] have not the same frequency");
        }

        Integer number1 = frequencies.get(element1);
        Integer number2 = frequencies.get(element2);
        if (!number1.equals(number2)) {
            throw new RuntimeException("Element [" + levelParser.elementsToChars.get(element1) + "] and [" + levelParser.elementsToChars.get(element2) + "] have not the same frequency");
        }
    }

    void setElement(byte mapElement, byte line, byte column) {
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

    void setStrength(byte strength, int line, int column) {
        gridStrengths[(line * width) + column] = strength;
        if (strength == 0) {
            canGoDown[line * width + column] = false;
            canGoUp[line * width + column] = false;
            canGoRight[line * width + column] = false;
            canGoLeft[line * width + column] = false;
            if (line != 0) {
                canGoDown[((line - 1) * width) + column] = false;
            }
            if (line != (height - 1)) {
                canGoUp[((line + 1) * width) + column] = false;
            }
            if (column != 0) {
                canGoRight[(line * width) + column - 1] = false;
            }
            if (column != (width - 1)) {
                canGoLeft[(line * width) + column + 1] = false;
            }
        }
    }

    void setBasement(byte basementType, int line, int column) {
        List<Integer> currentBasementTiles = basementTiles[basementType];
        if (currentBasementTiles == null) {
            currentBasementTiles = new ArrayList<>();
            basementTiles[basementType] = currentBasementTiles;
        }
        int position = (line * width) + column;
        currentBasementTiles.add(position);
    }

    void prepare() {
        int entryPosition = entryLine * width + entryColumn;
        byte[] initialGridStrengths = gridStrengths.clone();
        initialGridStrengths[entryPosition]--;

        MapState mapState = new MapState(
                this,
                entryPosition,
                MapElement.EMPTY_INDEX,
                initialGridStrengths,
                numberOfElements,
                new CoordinatesLinkedItem(entryPosition, null)
        );
        addMapState(mapState);
    }

    void addMapState(MapState mapState) {
        mapStates[mapStateNumber] = mapState;
        mapStateNumber++;
    }

    boolean hasMapStates() {
        return mapStateNumber != 0;
    }

    @NotNull MapState popState() {
        MapState mapState = mapStates[mapStateNumber - 1];
        mapStates[mapStateNumber - 1] = null;
        mapStateNumber--;
        return mapState;
    }


}
