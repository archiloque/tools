package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

final class MapElement {

    private MapElement(){
    }

    private final static int NUMBER_OF_ELEMENTS = 13;

    final static int EMPTY_INDEX = 0;
    final static int MONSTER_1_IN_FILLED_INDEX = 1;
    final static int MONSTER_1_IN_EMPTY_INDEX = 2;
    final static int MONSTER_2_IN_FILLED_INDEX = 3;
    final static int MONSTER_2_IN_EMPTY_INDEX = 4;
    final static int MONSTER_1_OUT_FILLED_INDEX = 5;
    final static int MONSTER_1_OUT_EMPTY_INDEX = 6;
    final static int MONSTER_2_OUT_FILLED_INDEX = 7;
    final static int MONSTER_2_OUT_EMPTY_INDEX = 8;
    final static int OBSTACLE_INDEX = 9;
    final static int RAIL_INDEX = 10;
    final static int ENTRY_INDEX = 11;
    final static int EXIT_INDEX = 12;

    final static boolean[] MONSTER_IN_FILLED = new boolean[NUMBER_OF_ELEMENTS];
    final static boolean[] CROSSABLE = new boolean[NUMBER_OF_ELEMENTS];
    final static int[] MONSTER = new int[NUMBER_OF_ELEMENTS];

    private static void setValues(int index,
                                  boolean monsterInFilled,
                                  boolean crossable,
                                  int monster) {
        MONSTER_IN_FILLED[index] = monsterInFilled;
        CROSSABLE[index] = crossable;
        MONSTER[index] = monster;
    }

    static {
        setValues(EMPTY_INDEX, false, true, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_1_IN_FILLED_INDEX, true, false, TrainElementContent.MONSTER_1);
        setValues(MONSTER_1_IN_EMPTY_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_2_IN_FILLED_INDEX, true, false, TrainElementContent.MONSTER_2);
        setValues(MONSTER_2_IN_EMPTY_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_1_OUT_FILLED_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_1_OUT_EMPTY_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_2_OUT_FILLED_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_2_OUT_EMPTY_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(OBSTACLE_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(RAIL_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(ENTRY_INDEX, false, false, TrainElementContent.NO_CONTENT);
        setValues(EXIT_INDEX, false, true, TrainElementContent.NO_CONTENT);

    }

    static int outBoardEmptyElement(int trainElementContent) {
        switch (trainElementContent) {
            case TrainElementContent.MONSTER_1:
                return MONSTER_1_OUT_EMPTY_INDEX;
            case TrainElementContent.MONSTER_2:
                return MONSTER_2_OUT_EMPTY_INDEX;
            default:
                throw new RuntimeException("Unknown content [" + trainElementContent + "]");
        }
    }

    static int outBoardFilledElement(int trainElementContent) {
        switch (trainElementContent) {
            case TrainElementContent.MONSTER_1:
                return MONSTER_1_OUT_FILLED_INDEX;
            case TrainElementContent.MONSTER_2:
                return MONSTER_2_OUT_FILLED_INDEX;
            default:
                throw new RuntimeException("Unknown content [" + trainElementContent + "]");
        }
    }

    static int inBoardEmptyElement(int trainElementContent) {
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
