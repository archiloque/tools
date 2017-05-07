package net.archiloque.cosmic_express;

final class MapElement {


    final static byte EMPTY_INDEX = (byte) 0;
    final static byte OBSTACLE_INDEX = EMPTY_INDEX + 1;
    final static byte RAIL_INDEX = OBSTACLE_INDEX + 1;
    final static byte ENTRY_INDEX = RAIL_INDEX + 1;
    final static byte EXIT_INDEX = ENTRY_INDEX + 1;

    final static byte MONSTER_PURPLE_IN_FILLED_INDEX = EXIT_INDEX + 1;
    final static byte MONSTER_PURPLE_IN_EMPTY_INDEX = MONSTER_PURPLE_IN_FILLED_INDEX + 1;
    final static byte MONSTER_PURPLE_OUT_FILLED_INDEX = MONSTER_PURPLE_IN_EMPTY_INDEX + 1;
    final static byte MONSTER_PURPLE_OUT_EMPTY_INDEX = MONSTER_PURPLE_OUT_FILLED_INDEX + 1;

    final static byte MONSTER_ORANGE_IN_FILLED_INDEX = MONSTER_PURPLE_OUT_EMPTY_INDEX + 1;
    final static byte MONSTER_ORANGE_IN_EMPTY_INDEX = MONSTER_ORANGE_IN_FILLED_INDEX + 1;
    final static byte MONSTER_ORANGE_OUT_FILLED_INDEX = MONSTER_ORANGE_IN_EMPTY_INDEX + 1;
    final static byte MONSTER_ORANGE_OUT_EMPTY_INDEX = MONSTER_ORANGE_OUT_FILLED_INDEX + 1;
    
    final static byte MONSTER_RED_OUT_EMPTY_INDEX = MONSTER_ORANGE_OUT_EMPTY_INDEX + 1;
    final static byte MONSTER_RED_OUT_FILLED_INDEX = MONSTER_RED_OUT_EMPTY_INDEX + 1;

    final static byte MONSTER_GREEN_IN_FILLED_INDEX = MONSTER_RED_OUT_EMPTY_INDEX + 1;
    final static byte MONSTER_GREEN_IN_EMPTY_INDEX = MONSTER_GREEN_IN_FILLED_INDEX + 1;
    final static byte MONSTER_GREEN_OUT_FILLED_INDEX = MONSTER_GREEN_IN_EMPTY_INDEX + 1;
    final static byte MONSTER_GREEN_OUT_EMPTY_INDEX = MONSTER_GREEN_OUT_FILLED_INDEX + 1;
    
    private final static int NUMBER_OF_ELEMENTS = MONSTER_GREEN_OUT_EMPTY_INDEX + 1;

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
        setValues(OBSTACLE_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(RAIL_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(ENTRY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(EXIT_INDEX, false, false, true, TrainElementContent.NO_CONTENT);

        setValues(MONSTER_PURPLE_IN_FILLED_INDEX, true, false, false, TrainElementContent.MONSTER_PURPLE);
        setValues(MONSTER_PURPLE_IN_EMPTY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_PURPLE_OUT_FILLED_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_PURPLE_OUT_EMPTY_INDEX, false, true, false, TrainElementContent.NO_CONTENT);

        setValues(MONSTER_ORANGE_IN_FILLED_INDEX, true, false, false, TrainElementContent.MONSTER_ORANGE);
        setValues(MONSTER_ORANGE_IN_EMPTY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_ORANGE_OUT_FILLED_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_ORANGE_OUT_EMPTY_INDEX, false, true, false, TrainElementContent.NO_CONTENT);

        setValues(MONSTER_RED_OUT_FILLED_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_RED_OUT_EMPTY_INDEX, false, true, false, TrainElementContent.NO_CONTENT);

        setValues(MONSTER_GREEN_IN_FILLED_INDEX, true, false, false, TrainElementContent.MONSTER_GREEN);
        setValues(MONSTER_GREEN_IN_EMPTY_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_GREEN_OUT_FILLED_INDEX, false, false, false, TrainElementContent.NO_CONTENT);
        setValues(MONSTER_GREEN_OUT_EMPTY_INDEX, false, true, false, TrainElementContent.NO_CONTENT);
    }

}
