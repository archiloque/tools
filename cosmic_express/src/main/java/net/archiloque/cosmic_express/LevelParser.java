package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

class LevelParser {

    final @NotNull Map<Byte, Character> elementsToChars = new HashMap<>();
    final @NotNull Map<Character, Byte> charsToElements = new HashMap<>();

    LevelParser() {
        elementsToChars.put(MapElement.ENTRY_INDEX, 'I');
        elementsToChars.put(MapElement.EXIT_INDEX, 'Q');
        elementsToChars.put(MapElement.EMPTY_INDEX, 'X');
        elementsToChars.put(MapElement.OBSTACLE_INDEX, 'O');

        elementsToChars.put(MapElement.MONSTER_1_IN_FILLED_INDEX, 'A');
        elementsToChars.put(MapElement.MONSTER_1_OUT_EMPTY_INDEX, 'a');
        elementsToChars.put(MapElement.MONSTER_2_IN_FILLED_INDEX, 'B');
        elementsToChars.put(MapElement.MONSTER_2_OUT_EMPTY_INDEX, 'b');
        elementsToChars.put(MapElement.RAIL_INDEX, 'R');

        charsToElements.put('I', MapElement.ENTRY_INDEX);
        charsToElements.put('Q', MapElement.EXIT_INDEX);
        charsToElements.put('X', MapElement.EMPTY_INDEX);
        charsToElements.put('O', MapElement.OBSTACLE_INDEX);

        charsToElements.put('A', MapElement.MONSTER_1_IN_FILLED_INDEX);
        charsToElements.put('a', MapElement.MONSTER_1_OUT_EMPTY_INDEX);
        charsToElements.put('B', MapElement.MONSTER_2_IN_FILLED_INDEX);
        charsToElements.put('b', MapElement.MONSTER_2_OUT_EMPTY_INDEX);
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
