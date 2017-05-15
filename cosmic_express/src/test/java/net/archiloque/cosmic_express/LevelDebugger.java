package net.archiloque.cosmic_express;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LevelDebugger {

    @Test
    public void testLevel1() {
        LevelParser levelParser = new LevelParser();
        Level level = levelParser.readLevel(("BXQXXXXXXIXA\n" +
                "XXXXcXXaXXXX\n" +
                "XXXXXXXXXXXX\n" +
                "XXXXXXXXXXXX\n" +
                "XXXXbXXcXXXX\n" +
                "AXXXXXXXXXXB").split("\n"), 2);
        Coordinates[] solutionPath = new Coordinates[]{
                new Coordinates(0,9),
                new Coordinates(0,10),
                new Coordinates(1,10),
                new Coordinates(1,9),
                new Coordinates(2,9),
                new Coordinates(2,8),
                new Coordinates(3,8),
                new Coordinates(4,8),
                new Coordinates(4,9),
                new Coordinates(4,10),
                new Coordinates(5,10),
                new Coordinates(5,9),
                new Coordinates(5,8),
                new Coordinates(5,7),
                new Coordinates(5,6),
                new Coordinates(5,5),
                new Coordinates(5,4),
                new Coordinates(5,3),
                new Coordinates(5,2),
                new Coordinates(5,1),
                new Coordinates(4,1),
                new Coordinates(4,2),
                new Coordinates(4,3),
                new Coordinates(3,3),
                new Coordinates(3,4),
                new Coordinates(3,5),
                new Coordinates(3,6),
                new Coordinates(3,7),
                new Coordinates(2,7),
                new Coordinates(2,6),
                new Coordinates(2,5),
                new Coordinates(2,4),
                new Coordinates(2,3),
                new Coordinates(2,2),
                new Coordinates(2,1),
                new Coordinates(2,0),
                new Coordinates(1,0),
                new Coordinates(1,1),
                new Coordinates(1,2),
                new Coordinates(1,3),
                new Coordinates(0,3),
                new Coordinates(0,2)
        };
        int currentSolutionIndex = solutionPath.length;

        List<MapState> mapStates = level.createMapStates();
        if (mapStates.size() != 1) {
            throw new RuntimeException("Several map states, error !");
        }

        List<Coordinates> coordinatesList = new ArrayList<>(currentSolutionIndex);
        for(int i = 0; i < currentSolutionIndex; i++) {
            coordinatesList.add(solutionPath[i]);
        }
        MapState.TRAIN_PATH_TO_CHECK = coordinatesList.toArray(new Coordinates[coordinatesList.size()]);
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
