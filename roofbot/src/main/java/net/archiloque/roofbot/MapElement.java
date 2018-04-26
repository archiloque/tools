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
    final static boolean[] CAN_GO = new boolean[NUMBER_OF_ELEMENTS*NUMBER_OF_ELEMENTS];

    private static void cantGoWithObject(byte index) {
        CAN_GO[GREEN_OBJECT_INDEX* NUMBER_OF_ELEMENTS + index] = false;
        CAN_GO[BLUE_OBJECT_INDEX* NUMBER_OF_ELEMENTS + index] = false;
        CAN_GO[YELLOW_OBJECT_INDEX* NUMBER_OF_ELEMENTS + index] = false;
        CAN_GO[RED_HOLE_INDEX* NUMBER_OF_ELEMENTS + index] = false;
    }

    static {
        Arrays.fill(CAN_GO, true);
        CAN_GO[EMPTY_INDEX* NUMBER_OF_ELEMENTS + GREEN_HOLE_INDEX] = false;
        CAN_GO[EMPTY_INDEX* NUMBER_OF_ELEMENTS + BLUE_HOLE_INDEX] = false;
        CAN_GO[EMPTY_INDEX* NUMBER_OF_ELEMENTS + YELLOW_HOLE_INDEX] = false;
        CAN_GO[EMPTY_INDEX* NUMBER_OF_ELEMENTS + RED_HOLE_INDEX] = false;

        CAN_GO[GREEN_OBJECT_INDEX* NUMBER_OF_ELEMENTS + BLUE_HOLE_INDEX] = false;
        CAN_GO[GREEN_OBJECT_INDEX* NUMBER_OF_ELEMENTS + YELLOW_HOLE_INDEX] = false;
        CAN_GO[GREEN_OBJECT_INDEX* NUMBER_OF_ELEMENTS + RED_HOLE_INDEX] = false;

        CAN_GO[BLUE_OBJECT_INDEX* NUMBER_OF_ELEMENTS + GREEN_HOLE_INDEX] = false;
        CAN_GO[BLUE_OBJECT_INDEX* NUMBER_OF_ELEMENTS + YELLOW_HOLE_INDEX] = false;
        CAN_GO[BLUE_OBJECT_INDEX* NUMBER_OF_ELEMENTS + RED_HOLE_INDEX] = false;

        CAN_GO[YELLOW_OBJECT_INDEX* NUMBER_OF_ELEMENTS + GREEN_HOLE_INDEX] = false;
        CAN_GO[YELLOW_OBJECT_INDEX* NUMBER_OF_ELEMENTS + BLUE_HOLE_INDEX] = false;
        CAN_GO[YELLOW_OBJECT_INDEX* NUMBER_OF_ELEMENTS + RED_HOLE_INDEX] = false;

        CAN_GO[RED_OBJECT_INDEX* NUMBER_OF_ELEMENTS + GREEN_HOLE_INDEX] = false;
        CAN_GO[RED_OBJECT_INDEX* NUMBER_OF_ELEMENTS + BLUE_HOLE_INDEX] = false;
        CAN_GO[RED_OBJECT_INDEX* NUMBER_OF_ELEMENTS + YELLOW_HOLE_INDEX] = false;

        cantGoWithObject(VOID_INDEX);
        cantGoWithObject(ENTRY_INDEX);
        cantGoWithObject(EXIT_INDEX);
        cantGoWithObject(GREEN_OBJECT_INDEX);
        cantGoWithObject(BLUE_OBJECT_INDEX);
        cantGoWithObject(YELLOW_OBJECT_INDEX);
        cantGoWithObject(RED_OBJECT_INDEX);

        CAN_GO[EMPTY_INDEX* NUMBER_OF_ELEMENTS + VOID_INDEX] = false;
        CAN_GO[EMPTY_INDEX* NUMBER_OF_ELEMENTS + ENTRY_INDEX] = false;

        CAN_GO[GREEN_OBJECT_INDEX* NUMBER_OF_ELEMENTS + BLUE_HOLE_INDEX] = false;
        CAN_GO[BLUE_OBJECT_INDEX* NUMBER_OF_ELEMENTS + YELLOW_HOLE_INDEX] = false;
        CAN_GO[YELLOW_OBJECT_INDEX* NUMBER_OF_ELEMENTS + RED_HOLE_INDEX] = false;
        CAN_GO[RED_HOLE_INDEX* NUMBER_OF_ELEMENTS + RED_HOLE_INDEX] = false;
    }


}
