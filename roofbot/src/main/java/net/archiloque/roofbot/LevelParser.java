package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

final class LevelParser {

    private static final char ENTRY_CHAR = 'I';
    private static final char EXIT_CHAR = 'O';
    private static final char EMPTY_CHAR = 'X';
    private static final char VOID_CHAR = '0';
    private static final char TRIGGER_1_CHAR = '⊤';
    private static final char TRIGGER_2_CHAR = '⊥';

    private static final char GREEN_OBJECT = 'G';
    private static final char GREEN_HOLE = 'g';
    
    private static final char BLUE_OBJECT = 'B';
    private static final char BLUE_HOLE = 'b';
    
    private static final char YELLOW_OBJECT = 'Y';
    private static final char YELLOW_HOLE = 'y';
    
    private static final char RED_OBJECT = 'R';
    private static final char RED_HOLE = 'r';


    private final @NotNull Map<Character, Byte> charsToElements = new HashMap<>();
    private final @NotNull Map<Character, Integer> charsToStrength = new HashMap<>();
    private final @NotNull Map<Character, Byte> charsToBasement = new HashMap<>();
    final @NotNull Map<Byte, Character> elementsToChars = new HashMap<>();

    LevelParser() {
        charsToElements.put(ENTRY_CHAR, MapElement.ENTRY_INDEX);
        elementsToChars.put(MapElement.ENTRY_INDEX, ENTRY_CHAR);

        charsToElements.put(VOID_CHAR, MapElement.VOID_INDEX);
        elementsToChars.put(MapElement.VOID_INDEX, VOID_CHAR);

        charsToElements.put(TRIGGER_1_CHAR, MapElement.TRIGGER_1_INDEX);
        elementsToChars.put(MapElement.TRIGGER_1_INDEX, TRIGGER_1_CHAR);

        charsToElements.put(TRIGGER_2_CHAR, MapElement.TRIGGER_2_INDEX);
        elementsToChars.put(MapElement.TRIGGER_2_INDEX, TRIGGER_2_CHAR);

        charsToElements.put(EXIT_CHAR, MapElement.EXIT_INDEX);
        elementsToChars.put(MapElement.EXIT_INDEX, EXIT_CHAR);

        charsToElements.put(EMPTY_CHAR, MapElement.EMPTY_INDEX);
        elementsToChars.put(MapElement.EMPTY_INDEX, EMPTY_CHAR);

        charsToElements.put(GREEN_OBJECT, MapElement.GREEN_OBJECT_INDEX);
        elementsToChars.put(MapElement.GREEN_OBJECT_INDEX, GREEN_OBJECT);

        charsToElements.put(GREEN_HOLE, MapElement.GREEN_HOLE_INDEX);
        elementsToChars.put(MapElement.GREEN_HOLE_INDEX, GREEN_HOLE);

        charsToElements.put(BLUE_OBJECT, MapElement.BLUE_OBJECT_INDEX);
        elementsToChars.put(MapElement.BLUE_OBJECT_INDEX, BLUE_OBJECT);

        charsToElements.put(BLUE_HOLE, MapElement.BLUE_HOLE_INDEX);
        elementsToChars.put(MapElement.BLUE_HOLE_INDEX, BLUE_HOLE);

        charsToElements.put(YELLOW_OBJECT, MapElement.YELLOW_OBJECT_INDEX);
        elementsToChars.put(MapElement.YELLOW_OBJECT_INDEX, YELLOW_OBJECT);

        charsToElements.put(YELLOW_HOLE, MapElement.YELLOW_HOLE_INDEX);
        elementsToChars.put(MapElement.YELLOW_HOLE_INDEX, YELLOW_HOLE);

        charsToElements.put(RED_OBJECT, MapElement.RED_OBJECT_INDEX);
        elementsToChars.put(MapElement.RED_OBJECT_INDEX, RED_OBJECT);

        charsToElements.put(RED_HOLE, MapElement.RED_HOLE_INDEX);
        elementsToChars.put(MapElement.RED_HOLE_INDEX, RED_HOLE);

        charsToStrength.put('0', 0);
        charsToStrength.put('1', 1);
        charsToStrength.put('2', 2);
        charsToBasement.put(TRIGGER_1_CHAR, MapElement.TRIGGER_1_INDEX);
        charsToBasement.put(TRIGGER_2_CHAR,  MapElement.TRIGGER_2_INDEX);
    }

    @NotNull Level readLevel(@NotNull String[] elements, @NotNull String[] strengths) {
        int linesNumber = elements.length;
        int columnsNumber = elements[0].length();

        if (strengths.length != linesNumber) {
            throw new RuntimeException("Lines number is incoherent : " + linesNumber + " for elements versus " + strengths.length + " for strength");
        }

        Level level = new Level(linesNumber, columnsNumber);
        for (int lineIndex = 0; lineIndex < elements.length; lineIndex++) {
            String line = elements[lineIndex];
            if (line.length() != columnsNumber) {
                throw new RuntimeException("Line " + lineIndex + " with content [" + line + "] is " + line.length() + " instead of " + columnsNumber + " like the first line");
            }
            for (int columnIndex = 0; columnIndex < columnsNumber; columnIndex++) {
                Character elementChar = line.charAt(columnIndex);
                Byte elementElement = charsToElements.get(elementChar);
                if (elementElement != null) {
                    level.setElement(elementElement, lineIndex, columnIndex);
                } else {
                    throw new RuntimeException("Unknown element [" + elementChar + "]");
                }
            }
        }

        for (int lineIndex = 0; lineIndex < strengths.length; lineIndex++) {
            String line = strengths[lineIndex];
            if (line.length() != columnsNumber) {
                throw new RuntimeException("Line " + lineIndex + " with content [" + line + "] is " + line.length() + " instead of " + columnsNumber + " like the first line");
            }
            for (int columnIndex = 0; columnIndex < columnsNumber; columnIndex++) {
                Character elementChar = line.charAt(columnIndex);
                Integer elementStrength = charsToStrength.get(elementChar);
                if (elementStrength != null) {
                    level.setStrength(elementStrength, lineIndex, columnIndex);
                } else {
                    Byte basementType = charsToBasement.get(elementChar);
                    if (basementType != null) {
                        level.setBasement(basementType, lineIndex, columnIndex);
                    } else {

                        throw new RuntimeException("Unknown strength [" + elementChar + "]");
                    }

                }
            }
        }

        return level;
    }
}
