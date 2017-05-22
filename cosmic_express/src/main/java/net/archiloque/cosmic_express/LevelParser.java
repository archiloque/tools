package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

final class LevelParser {

    private static final char ENTRY_CHAR = 'I';
    private static final char EXIT_CHAR = 'Q';
    private static final char EMPTY_CHAR = 'X';
    private static final char OBSTACLE_CHAR = 'O';
    private static final char RAIL_CHAR = 'R';

    private static final char MONSTER_PURPLE_IN_FILLED_CHAR = 'A';
    private static final char MONSTER_PURPLE_OUT_EMPTY_CHAR = 'a';
    private static final char MONSTER_ORANGE_IN_FILLED_CHAR = 'B';
    private static final char MONSTER_ORANGE_OUT_EMPTY_CHAR = 'b';
    private static final char MONSTER_RED_OUT_EMPTY_CHAR = 'c';
    private static final char MONSTER_GREEN_IN_FILLED_CHAR = 'D';
    private static final char MONSTER_GREEN_OUT_EMPTY_CHAR = 'd';

    private static final char I_MONSTER_PURPLE_IN_FILLED_CHAR = 'Ⓐ';
    private static final char I_MONSTER_PURPLE_OUT_EMPTY_CHAR = 'ⓐ';
    private static final char I_MONSTER_ORANGE_IN_FILLED_CHAR = 'Ⓑ';
    private static final char I_MONSTER_ORANGE_OUT_EMPTY_CHAR = 'ⓑ';
    private static final char I_MONSTER_RED_OUT_EMPTY_CHAR = 'ⓒ';
    private static final char I_MONSTER_GREEN_IN_FILLED_CHAR = 'Ⓓ';
    private static final char I_MONSTER_GREEN_OUT_EMPTY_CHAR = 'ⓓ';

    final @NotNull Map<Byte, Character> elementsToChars = new HashMap<>();
    final @NotNull Map<Byte, Character> elementsToInvertChar = new HashMap<>();
    private final @NotNull Map<Character, Byte> charsToElements = new HashMap<>();

    LevelParser() {
        elementsToChars.put(MapElement.ENTRY_INDEX, ENTRY_CHAR);
        elementsToChars.put(MapElement.EXIT_INDEX, EXIT_CHAR);
        elementsToChars.put(MapElement.EMPTY_INDEX, EMPTY_CHAR);
        elementsToChars.put(MapElement.OBSTACLE_INDEX, OBSTACLE_CHAR);
        elementsToChars.put(MapElement.RAIL_INDEX, RAIL_CHAR);

        elementsToChars.put(MapElement.MONSTER_PURPLE_IN_FILLED_INDEX, MONSTER_PURPLE_IN_FILLED_CHAR);
        elementsToChars.put(MapElement.MONSTER_PURPLE_OUT_EMPTY_INDEX, MONSTER_PURPLE_OUT_EMPTY_CHAR);

        elementsToChars.put(MapElement.MONSTER_ORANGE_IN_FILLED_INDEX, MONSTER_ORANGE_IN_FILLED_CHAR);
        elementsToChars.put(MapElement.MONSTER_ORANGE_OUT_EMPTY_INDEX, MONSTER_ORANGE_OUT_EMPTY_CHAR);

        elementsToChars.put(MapElement.MONSTER_RED_OUT_EMPTY_INDEX, MONSTER_RED_OUT_EMPTY_CHAR);

        elementsToChars.put(MapElement.MONSTER_GREEN_IN_FILLED_INDEX, MONSTER_GREEN_IN_FILLED_CHAR);
        elementsToChars.put(MapElement.MONSTER_GREEN_OUT_EMPTY_INDEX, MONSTER_GREEN_OUT_EMPTY_CHAR);

        elementsToInvertChar.put(MapElement.MONSTER_PURPLE_IN_FILLED_INDEX, I_MONSTER_PURPLE_IN_FILLED_CHAR);
        elementsToInvertChar.put(MapElement.MONSTER_PURPLE_OUT_EMPTY_INDEX, I_MONSTER_PURPLE_OUT_EMPTY_CHAR);

        elementsToInvertChar.put(MapElement.MONSTER_ORANGE_IN_FILLED_INDEX, I_MONSTER_ORANGE_IN_FILLED_CHAR);
        elementsToInvertChar.put(MapElement.MONSTER_ORANGE_OUT_EMPTY_INDEX, I_MONSTER_ORANGE_OUT_EMPTY_CHAR);

        elementsToInvertChar.put(MapElement.MONSTER_RED_OUT_EMPTY_INDEX, I_MONSTER_RED_OUT_EMPTY_CHAR);

        elementsToInvertChar.put(MapElement.MONSTER_GREEN_IN_FILLED_INDEX, I_MONSTER_GREEN_IN_FILLED_CHAR);
        elementsToInvertChar.put(MapElement.MONSTER_GREEN_OUT_EMPTY_INDEX, I_MONSTER_GREEN_OUT_EMPTY_CHAR);

        charsToElements.put(ENTRY_CHAR, MapElement.ENTRY_INDEX);
        charsToElements.put(EXIT_CHAR, MapElement.EXIT_INDEX);
        charsToElements.put(EMPTY_CHAR, MapElement.EMPTY_INDEX);
        charsToElements.put(OBSTACLE_CHAR, MapElement.OBSTACLE_INDEX);

        charsToElements.put(MONSTER_PURPLE_IN_FILLED_CHAR, MapElement.MONSTER_PURPLE_IN_FILLED_INDEX);
        charsToElements.put(MONSTER_PURPLE_OUT_EMPTY_CHAR, MapElement.MONSTER_PURPLE_OUT_EMPTY_INDEX);

        charsToElements.put(MONSTER_ORANGE_IN_FILLED_CHAR, MapElement.MONSTER_ORANGE_IN_FILLED_INDEX);
        charsToElements.put(MONSTER_ORANGE_OUT_EMPTY_CHAR, MapElement.MONSTER_ORANGE_OUT_EMPTY_INDEX);

        charsToElements.put(MONSTER_RED_OUT_EMPTY_CHAR, MapElement.MONSTER_RED_OUT_EMPTY_INDEX);

        charsToElements.put(MONSTER_GREEN_IN_FILLED_CHAR, MapElement.MONSTER_GREEN_IN_FILLED_INDEX);
        charsToElements.put(MONSTER_GREEN_OUT_EMPTY_CHAR, MapElement.MONSTER_GREEN_OUT_EMPTY_INDEX);
    }

    @NotNull Level readLevel(@NotNull String[] content, int trainSize) {
        int linesNumber = content.length;
        int columnsNumber = content[0].length();

        Level level = new Level(trainSize, linesNumber, columnsNumber);
        int entryNumber = 0;
        int exitNumber = 0;
        for (int lineIndex = 0; lineIndex < content.length; lineIndex++) {
            String line = content[lineIndex];
            if (line.length() != columnsNumber) {
                throw new RuntimeException("Line " + lineIndex + " with content [" + line + "] is " + line.length() + " instead of " + columnsNumber + " like the first line");
            }
            for (int columnIndex = 0; columnIndex < columnsNumber; columnIndex++) {
                Character elementChar = line.charAt(columnIndex);
                Byte elementElement = charsToElements.get(elementChar);
                if (elementElement != null) {
                    level.setElement(elementElement, new Coordinates(lineIndex, columnIndex));
                    if (elementElement == MapElement.ENTRY_INDEX) {
                        entryNumber++;
                    } else if (elementElement == MapElement.EXIT_INDEX) {
                        exitNumber++;
                    }
                } else {
                    throw new RuntimeException("Unknown element [" + elementChar + "]");
                }

            }
        }
        if (exitNumber < 1) {
            throw new RuntimeException("Not enough exits");
        }
        if (entryNumber == 0) {
            throw new RuntimeException("Bad number of entry");
        }

        return level;
    }


}
