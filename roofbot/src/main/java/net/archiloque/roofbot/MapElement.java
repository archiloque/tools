package net.archiloque.roofbot;

import java.util.Arrays;
import java.util.Collection;

final class MapElement {

    final static byte EMPTY_INDEX = (byte) 0;
    final static byte VOID_INDEX = EMPTY_INDEX + 1;
    final static byte ENTRY_INDEX = VOID_INDEX + 1;
    final static byte EXIT_INDEX = ENTRY_INDEX + 1;
    final static byte TRIGGER_1_INDEX = EXIT_INDEX + 1;
    final static byte TRIGGER_2_INDEX = TRIGGER_1_INDEX + 1;

    final static byte GREEN_OBJECT_INDEX = TRIGGER_2_INDEX + 1;
    final static byte GREEN_HOLE_INDEX = GREEN_OBJECT_INDEX + 1;

    final static byte BLUE_OBJECT_INDEX = GREEN_HOLE_INDEX + 1;
    final static byte BLUE_HOLE_INDEX = BLUE_OBJECT_INDEX + 1;

    final static byte YELLOW_OBJECT_INDEX = BLUE_HOLE_INDEX + 1;
    final static byte YELLOW_HOLE_INDEX = YELLOW_OBJECT_INDEX + 1;

    final static byte RED_OBJECT_INDEX = YELLOW_HOLE_INDEX + 1;
    final static byte RED_HOLE_INDEX = RED_OBJECT_INDEX + 1;

    final static int NUMBER_OF_ELEMENTS = RED_HOLE_INDEX + 1;

    final static Collection<Byte> HOLES = Arrays.asList(
            GREEN_HOLE_INDEX,
            BLUE_HOLE_INDEX,
            YELLOW_HOLE_INDEX,
            RED_HOLE_INDEX
    );

    final static boolean[] CAN_GO_HOLDING_NOTHING = new boolean[NUMBER_OF_ELEMENTS];
    final static boolean[] CAN_GO_HOLDING_SOMETHING = new boolean[NUMBER_OF_ELEMENTS];
    final static byte[] HOLE_FOR_ELEMENT = new byte[NUMBER_OF_ELEMENTS];
    final static boolean[] CAN_BE_PICKED = new boolean[NUMBER_OF_ELEMENTS];
    final static boolean[] CAN_WALK_FREELY = new boolean[NUMBER_OF_ELEMENTS];
    final static boolean[] IS_TRIGGER = new boolean[NUMBER_OF_ELEMENTS];

    private static void setValues(int index,
                                  boolean canGoHoldingNothing,
                                  boolean canGoHoldingSomething,
                                  byte holeForElement,
                                  boolean canBePicked,
                                  boolean canWalkFreely,
                                  boolean isTrigger) {
        CAN_GO_HOLDING_NOTHING[index] = canGoHoldingNothing;
        CAN_GO_HOLDING_SOMETHING[index] = canGoHoldingSomething;
        HOLE_FOR_ELEMENT[index] = holeForElement;
        CAN_BE_PICKED[index] = canBePicked;
        CAN_WALK_FREELY[index] = canWalkFreely;
        IS_TRIGGER[index] = isTrigger;
    }

    static {
        setValues(EMPTY_INDEX, true, true, EMPTY_INDEX, false, true, false);
        setValues(VOID_INDEX, false, true, EMPTY_INDEX, false, false, false);
        setValues(ENTRY_INDEX, false, false, EMPTY_INDEX, false, false, false);
        setValues(EXIT_INDEX, true, false, EMPTY_INDEX, false, false, false);
        setValues(TRIGGER_1_INDEX, true, true, EMPTY_INDEX, false, true, true);
        setValues(TRIGGER_2_INDEX, true, true, EMPTY_INDEX, false, true, true);
        setValues(GREEN_OBJECT_INDEX, true, false, EMPTY_INDEX, true, false, false);
        setValues(GREEN_HOLE_INDEX, false, true, GREEN_OBJECT_INDEX, false, false, false);
        setValues(BLUE_OBJECT_INDEX, true, false, EMPTY_INDEX, true, false, false);
        setValues(BLUE_HOLE_INDEX, false, true, BLUE_OBJECT_INDEX, false, false, false);
        setValues(YELLOW_OBJECT_INDEX, true, false, EMPTY_INDEX, true, false, false);
        setValues(YELLOW_HOLE_INDEX, false, true, YELLOW_OBJECT_INDEX, false, false, false);
        setValues(RED_OBJECT_INDEX, true, false, EMPTY_INDEX, true, false, false);
        setValues(RED_HOLE_INDEX, false, true, RED_OBJECT_INDEX, false, false, false);
    }


}
