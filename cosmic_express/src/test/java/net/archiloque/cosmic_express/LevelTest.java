package net.archiloque.cosmic_express;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LevelTest
{
    @Test
    public void testLevel1() {
        LevelParser levelParser = new LevelParser();
        Level level = levelParser.readLevel(("XXXXXXXX\n" +
                "XXXXXXXX\n" +
                "XAXXXXXX\n"+
                "IXXXXXXQ\n"+
                "XXXXXXaX\n"+
                "XXXXXXXX\n" +
                "XXXXXXXX").split("\n"), 2);
        LinkedList<MapState> states = new LinkedList<>();
        List<MapState> mapStates = level.createMapStates();
        assertEquals(mapStates.size(), 1);
        states.add(mapStates.get(0));
        boolean solution = false;
        while((! solution) && (! states.isEmpty())){
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if(solution) {
                //assertArrayEquals(nextCandidate.previousTrainPath, new Direction[]{Direction.RIGHT, Direction.RIGHT, Direction.RIGHT, Direction.RIGHT, Direction.RIGHT, Direction.RIGHT, Direction.RIGHT});
            }
        }

    }
}
