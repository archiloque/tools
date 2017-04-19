package net.archiloque.cosmic_express;

final class MapElement {

    private final static int NUMBER_OF_ELEMENTS = 13;

    final static byte EMPTY_INDEX = 0;
    final static byte MONSTER_1_IN_FILLED_INDEX = 1;
    final static byte MONSTER_1_IN_EMPTY_INDEX = 2;
    final static byte MONSTER_2_IN_FILLED_INDEX = 3;
    final static byte MONSTER_2_IN_EMPTY_INDEX = 4;
    final static byte MONSTER_1_OUT_FILLED_INDEX = 5;
    final static byte MONSTER_1_OUT_EMPTY_INDEX = 6;
    final static byte MONSTER_2_OUT_FILLED_INDEX = 7;
    final static byte MONSTER_2_OUT_EMPTY_INDEX = 8;
    final static byte OBSTACLE_INDEX = 9;
    final static byte RAIL_INDEX = 10;
    final static byte ENTRY_INDEX = 11;
    final static byte EXIT_INDEX = 12;

    final static boolean[] MONSTER_IN_FILLED = new boolean[NUMBER_OF_ELEMENTS];
    final static boolean[] MONSTER_OUT_EMPTY = new boolean[NUMBER_OF_ELEMENTS];
    final static boolean[] CROSSABLE = new boolean[NUMBER_OF_ELEMENTS];
    final static byte[] MONSTER = new byte[NUMBER_OF_ELEMENTS];

    private static void setValues(int index,
                                  boolean monsterInFilled,
                                  boolean monsterOutEmpty,
                                  boolean crossable,
                                  byte monster) {
        MONSTER_IN_FILLED[index] = monsterInFilled;
        MONSTER_OUT_EMPTY[index] = monsterOutEmpty;
        CROSSABLE[index] = crossable;
        MONSTER[index] = monster;
    }

    static {
        setValues(EMPTY_INDEX, false, false, true, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_1_IN_FILLED_INDEX, true, false, false, TrainElementContent.MONSTER_1);
        setValues(MONSTER_1_IN_EMPTY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_2_IN_FILLED_INDEX, true, false, false, TrainElementContent.MONSTER_2);
        setValues(MONSTER_2_IN_EMPTY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_1_OUT_FILLED_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_1_OUT_EMPTY_INDEX, false, true, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_2_OUT_FILLED_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_2_OUT_EMPTY_INDEX, false, true, false, TrainElementContent.NO_CONTENT);
        setValues(OBSTACLE_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(RAIL_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(ENTRY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(EXIT_INDEX, false, false, true, TrainElementContent.NO_CONTENT);

    }

    static byte outBoardEmptyElement(byte trainElementContent) {
        switch (trainElementContent) {
            case TrainElementContent.MONSTER_1:
                return MONSTER_1_OUT_EMPTY_INDEX;
            case TrainElementContent.MONSTER_2:
                return MONSTER_2_OUT_EMPTY_INDEX;
            default:
                throw new RuntimeException("Unknown content [" + trainElementContent + "]");
        }
    }

    static byte outBoardFilledElement(byte trainElementContent) {
        switch (trainElementContent) {
            case TrainElementContent.MONSTER_1:
                return MONSTER_1_OUT_FILLED_INDEX;
            case TrainElementContent.MONSTER_2:
                return MONSTER_2_OUT_FILLED_INDEX;
            default:
                throw new RuntimeException("Unknown content [" + trainElementContent + "]");
        }
    }

    static byte inBoardEmptyElement(byte trainElementContent) {
        switch (trainElementContent) {
            case TrainElementContent.MONSTER_1:
                return MONSTER_1_IN_EMPTY_INDEX;
            case TrainElementContent.MONSTER_2:
                return MONSTER_2_IN_EMPTY_INDEX;
            default:
                throw new RuntimeException("Unknown content [" + trainElementContent + "]");
        }
    }
}
