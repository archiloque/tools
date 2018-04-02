package net.archiloque.roofbot;

import java.util.Arrays;
import java.util.Collection;

class MapElement {

    final static byte EMPTY_INDEX = 0;
    final static byte VOID_INDEX = EMPTY_INDEX + 1;
    final static byte ENTRY_INDEX = VOID_INDEX + 1;
    final static byte EXIT_INDEX = ENTRY_INDEX + 1;

    final static byte FAN_INDEX = EXIT_INDEX + 1;
    final static byte TELEPORTER_1_INDEX = FAN_INDEX + 1;
    final static byte TELEPORTER_2_INDEX = TELEPORTER_1_INDEX + 1;

    final static byte TRIGGER_1_INDEX = TELEPORTER_2_INDEX + 1;
    final static byte TRIGGER_2_INDEX = TRIGGER_1_INDEX + 1;

    final static byte GREEN_OBJECT_INDEX = TRIGGER_2_INDEX + 1;
    final static byte BLUE_OBJECT_INDEX = GREEN_OBJECT_INDEX + 1;
    final static byte YELLOW_OBJECT_INDEX = BLUE_OBJECT_INDEX + 1;
    final static byte RED_OBJECT_INDEX = YELLOW_OBJECT_INDEX + 1;

    final static byte GREEN_HOLE_INDEX = RED_OBJECT_INDEX + 1;
    final static byte BLUE_HOLE_INDEX = GREEN_HOLE_INDEX + 1;
    final static byte YELLOW_HOLE_INDEX = BLUE_HOLE_INDEX + 1;
    final static byte RED_HOLE_INDEX = YELLOW_HOLE_INDEX + 1;

    final static byte NUMBER_OF_ELEMENTS = RED_HOLE_INDEX + 1;

    final static Collection<Byte> HOLES = Arrays.asList(
            GREEN_HOLE_INDEX,
            BLUE_HOLE_INDEX,
            YELLOW_HOLE_INDEX,
            RED_HOLE_INDEX
    );

    final static boolean[] CAN_GO_HOLDING_NOTHING = new boolean[NUMBER_OF_ELEMENTS];
    final static byte[] HOLE_FOR_ELEMENT = new byte[NUMBER_OF_ELEMENTS];
    final static boolean[] CAN_WALK_FREELY = new boolean[NUMBER_OF_ELEMENTS];

    private static void setValues(int index,
                                  boolean canGoHoldingNothing,
                                  byte holeForElement,
                                  boolean canWalkFreely) {
        CAN_GO_HOLDING_NOTHING[index] = canGoHoldingNothing;
        HOLE_FOR_ELEMENT[index] = holeForElement;
        CAN_WALK_FREELY[index] = canWalkFreely;
    }

    static {
        setValues(EMPTY_INDEX, true, EMPTY_INDEX, true);
        setValues(VOID_INDEX, false, EMPTY_INDEX, false);
        setValues(ENTRY_INDEX, false, EMPTY_INDEX, false);
        setValues(EXIT_INDEX, true, EMPTY_INDEX, false);
        setValues(FAN_INDEX, true, EMPTY_INDEX, true);
        setValues(TELEPORTER_1_INDEX, true, EMPTY_INDEX, true);
        setValues(TELEPORTER_2_INDEX, true, EMPTY_INDEX, true);

        setValues(TRIGGER_1_INDEX, true, EMPTY_INDEX, true);
        setValues(TRIGGER_2_INDEX, true, EMPTY_INDEX, true);

        setValues(GREEN_OBJECT_INDEX, true, EMPTY_INDEX, false);
        setValues(GREEN_HOLE_INDEX, false, GREEN_OBJECT_INDEX, false);

        setValues(BLUE_OBJECT_INDEX, true, EMPTY_INDEX, false);
        setValues(BLUE_HOLE_INDEX, false, BLUE_OBJECT_INDEX, false);

        setValues(YELLOW_OBJECT_INDEX, true, EMPTY_INDEX, false);
        setValues(YELLOW_HOLE_INDEX, false, YELLOW_OBJECT_INDEX, false);

        setValues(RED_OBJECT_INDEX, true, EMPTY_INDEX, false);
        setValues(RED_HOLE_INDEX, false, RED_OBJECT_INDEX, false);
    }


}
