package net.archiloque.cosmic_express;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LevelDebugger {

    @Test
    public void testLevel1() {
        LevelParser levelParser = new LevelParser();
        Level level = levelParser.readLevel(("XXXXXXXXX\n" +
                "XXBBXXXbX\n" +
                "XXXXXXXbX\n" +
                "IXXXXXXXQ\n" +
                "XXXXXXXaX\n" +
                "XXAAXXXaX\n" +
                "XXXXXXXXX").split("\n"), 2);
        int[][] solutionPath = new int[][]{
                new int[]{3, 0},
                new int[]{3, 1},
                new int[]{4, 1},
                new int[]{5, 1},
                new int[]{6, 1},
                new int[]{6, 2},
                new int[]{6, 3},
                new int[]{6, 4},
                new int[]{6, 5},
                new int[]{6, 6},
                new int[]{5, 6},
                new int[]{5, 5},
                new int[]{5, 4},
                new int[]{4, 4},
                new int[]{4, 5},
                new int[]{4, 6},
                new int[]{3, 6},
                new int[]{3, 5},
                new int[]{3, 4},
                new int[]{3, 3},
                new int[]{3, 2},
                new int[]{2, 2},
                new int[]{2, 3},
                new int[]{2, 4},
                new int[]{2, 5},
                new int[]{2, 6},
                new int[]{1, 6},
                new int[]{1, 5},
                new int[]{1, 4},
                new int[]{0, 4},
                new int[]{0, 5},
                new int[]{0, 6},
                new int[]{0, 7},
                new int[]{0, 8},
                new int[]{1, 8},
                new int[]{2, 8}
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
