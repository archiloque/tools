package net.archiloque.cosmic_express;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LevelDebugger {

    @Test
    public void testLevel1() {
        LevelParser levelParser = new LevelParser();
        Level level = levelParser.readLevel(("XXXXXXXIXA\n" +
                "XXbXXcXXXA\n" +
                "QXXXXXXXXX\n" +
                "XXaXXcXXXB\n" +
                "XXXXXXXXXB").split("\n"), 3);
        Coordinates[] solutionPath = new Coordinates[]{
                new Coordinates(0,7),
                new Coordinates(0,8),
                new Coordinates(1,8),
                new Coordinates(1,7),
                new Coordinates(1,6),
                new Coordinates(0,6),
                new Coordinates(0,5),
                new Coordinates(0,4),
                new Coordinates(0,3),
                new Coordinates(1,3),
                new Coordinates(2,3),
                new Coordinates(3,3),
                new Coordinates(3,4),
                new Coordinates(2,4),
                new Coordinates(2,5),
                new Coordinates(2,6),
                new Coordinates(2,7),
                new Coordinates(2,8),
                new Coordinates(3,8),
                new Coordinates(3,7),
                new Coordinates(3,6),
                new Coordinates(3,5),
                new Coordinates(3,4),
                new Coordinates(3,3),
                new Coordinates(3,2),
                new Coordinates(3,1),
                new Coordinates(2,1),
                new Coordinates(1,1),
                new Coordinates(1,0),
                new Coordinates(2,0)
        };
        int currentSolutionIndex = solutionPath.length;
        while ((currentSolutionIndex > 0) && (! MapState.FOUND_TRAIN_PATH)) {
            System.out.println("Testing with " + currentSolutionIndex);
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
            while ((!solution) && (!states.isEmpty()) && (! MapState.FOUND_TRAIN_PATH)) {
                MapState nextCandidate = states.pop();
                solution = nextCandidate.processState(states);
                if(solution) {
                    nextCandidate.printableGrid();
                }
            }
            if((!solution) && (! MapState.FOUND_TRAIN_PATH)){
                currentSolutionIndex--;
            }

        }
        if (MapState.FOUND_TRAIN_PATH) {
            System.out.println("Best solution has index " + currentSolutionIndex);
        }

    }
}
