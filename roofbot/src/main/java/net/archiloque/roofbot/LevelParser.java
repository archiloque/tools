package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

final class LevelParser {

    private static final char ENTRY_CHAR = 'I';
    private static final char EXIT_CHAR = 'O';
    private static final char EMPTY_CHAR = 'X';
    private static final char VOID_CHAR = '0';
    private static final char FAN_CHAR = 'F';
    
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
        addElement(ENTRY_CHAR,MapElement.ENTRY_INDEX);
        addElement(EXIT_CHAR, MapElement.EXIT_INDEX);
        addElement(EMPTY_CHAR, MapElement.EMPTY_INDEX);
        addElement(VOID_CHAR, MapElement.VOID_INDEX);
        addElement(FAN_CHAR, MapElement.FAN_INDEX);
        
        addElement(TRIGGER_1_CHAR, MapElement.TRIGGER_1_INDEX);
        addElement(TRIGGER_2_CHAR, MapElement.TRIGGER_2_INDEX);
        
        addElement(GREEN_OBJECT, MapElement.GREEN_OBJECT_INDEX);
        addElement(GREEN_HOLE, MapElement.GREEN_HOLE_INDEX);

        addElement(BLUE_OBJECT, MapElement.BLUE_OBJECT_INDEX);
        addElement(BLUE_HOLE, MapElement.BLUE_HOLE_INDEX);

        addElement(YELLOW_OBJECT, MapElement.YELLOW_OBJECT_INDEX);
        addElement(YELLOW_HOLE, MapElement.YELLOW_HOLE_INDEX);

        addElement(RED_OBJECT, MapElement.RED_OBJECT_INDEX);
        addElement(RED_HOLE, MapElement.RED_HOLE_INDEX);

        charsToStrength.put('0', 0);
        charsToStrength.put('1', 1);
        charsToStrength.put('2', 2);
        charsToBasement.put(TRIGGER_1_CHAR, MapElement.TRIGGER_1_INDEX);
        charsToBasement.put(TRIGGER_2_CHAR,  MapElement.TRIGGER_2_INDEX);
    }
    
    private void addElement(char character, byte index) {
        charsToElements.put(character, index);
        elementsToChars.put(index, character);
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
