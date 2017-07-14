package net.archiloque.cosmic_express;

import org.junit.Test;

import java.util.List;

public class LevelDebugger {

    @Test
    public void testLevel1() {
        LevelParser levelParser = new LevelParser();
        Level level = levelParser.readLevel((
                "XXXIXXXQXXX\n" +
                "XDXXXXXXXdX\n" +
                "XXXXXXXXXXX\n" +
                "XXXXXXXXXXX\n" +
                "XAXXXbXXXXX\n" +
                "XXXXXXXXXXX\n" +
                "XXXXXBXXXXX\n" +
                "XdXXXXXXXdX\n" +
                "XXXXXXXXXXX").split("\n"), 2);
        Coordinates[] solutionPath = new Coordinates[]{
                new Coordinates(4,0),
                new Coordinates(5,0),
                new Coordinates(6,0),
                new Coordinates(7,0),
                new Coordinates(8,0),
                new Coordinates(8,1),
                new Coordinates(8,2),
                new Coordinates(8,3),
                new Coordinates(8,4),
                new Coordinates(8,5),
                new Coordinates(8,6),
                new Coordinates(7,6),
                new Coordinates(7,5),
                new Coordinates(7,4),
                new Coordinates(7,3),
                new Coordinates(6,3),
                new Coordinates(5,3),
                new Coordinates(5,2),
                new Coordinates(5,1),
                new Coordinates(4,1),
                new Coordinates(3,1),
                new Coordinates(3,0),
                new Coordinates(2,0),
                new Coordinates(1,0),
                new Coordinates(0,0),
                new Coordinates(0,1),
                new Coordinates(0,2),
                new Coordinates(0,3),
                new Coordinates(0,4),
                new Coordinates(0,5),
                new Coordinates(0,6),
                new Coordinates(1,6),
                new Coordinates(1,5),
                new Coordinates(1,4),
                new Coordinates(1,3),
                new Coordinates(2,3),
                new Coordinates(3,3),
                new Coordinates(3,4),
                new Coordinates(3,5),
                new Coordinates(4,5),
                new Coordinates(5,5),
                new Coordinates(5,6),
                new Coordinates(5,7),
                new Coordinates(5,8),
                new Coordinates(4,8),
                new Coordinates(4,7),
                new Coordinates(3,7),
                new Coordinates(2,7),
                new Coordinates(1,7),
                new Coordinates(1,8),
                new Coordinates(1,9),
                new Coordinates(2,9),
                new Coordinates(3,9),
                new Coordinates(4,9)

        };
        List<MapState> mapStates = level.createMapStates();
        if (mapStates.size() != 1) {
            throw new RuntimeException("Several map states, error !");
        }
        MapState.TRAIN_PATH_TO_CHECK = solutionPath;
        LinkedList<MapState> states = new LinkedList<>();
        states.add(mapStates.get(0));

        boolean solution = false;
        while ((!solution) && (!states.isEmpty())) {
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if(solution) {
                nextCandidate.printableGrid();
            }
        }
        System.out.println("Best solution has index " + MapState.MAX_COMMON_PATH_LENGTH);

    }
}
