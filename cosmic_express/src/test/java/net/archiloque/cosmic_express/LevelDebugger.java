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
        int[][] solutionPath = new int[][]{
                new int[]{0,7},
                new int[]{0,8},
                new int[]{1,8},
                new int[]{1,7},
                new int[]{1,6},
                new int[]{0,6},
                new int[]{0,5},
                new int[]{0,4},
                new int[]{0,3},
                new int[]{1,3},
                new int[]{2,3},
                new int[]{3,3},
                new int[]{3,4},
                new int[]{2,4},
                new int[]{2,5},
                new int[]{2,6},
                new int[]{2,7},
                new int[]{2,8},
                new int[]{3,8},
                new int[]{3,7},
                new int[]{3,6},
                new int[]{3,5},
                new int[]{3,4},
                new int[]{3,3},
                new int[]{3,2},
                new int[]{3,1},
                new int[]{2,1},
                new int[]{1,1},
                new int[]{1,0},
                new int[]{2,0}
        };
        int currentSolutionIndex = solutionPath.length;
        while ((currentSolutionIndex > 0) && (! MapState.FOUND_TRAIN_PATH)) {
            System.out.println("Testing with " + currentSolutionIndex);
            List<MapState> mapStates = level.createMapStates();
            if (mapStates.size() != 1) {
                throw new RuntimeException("Several map states, error !");
            }

            List<Integer> coordinatesList = new ArrayList<>(currentSolutionIndex);
            for(int i = 0; i < currentSolutionIndex; i++) {
                coordinatesList.add((solutionPath[i][0] << 16) + solutionPath[i][1]);
            }
            MapState.TRAIN_PATH_TO_CHECK = Level.listToPrimitiveIntArray(coordinatesList);

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
