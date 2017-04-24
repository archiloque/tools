package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class App {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("We need one parameter for the problems lists");
        }
        String fileName = args[0];
        System.out.println("Using problems from [" + fileName + "]");
        Path problemsFile = Paths.get(fileName);
        if (!Files.exists(problemsFile)) {
            System.out.println("Problem file doesn't exist [" + problemsFile.getFileName() + "]");
        }
        LevelFileParser levelFileParser = new LevelFileParser();
        Map<String, Level> stringLevelMap = levelFileParser.parseFile(problemsFile);
        String[] levelNames = stringLevelMap.keySet().toArray(new String[stringLevelMap.size()]);
        Arrays.sort(levelNames);

        try (BufferedWriter resultWriter = Files.newBufferedWriter(Paths.get(fileName.substring(0, fileName.length() - 4) + "-solutions.txt"))) {
            for (String levelName : levelNames) {
                Level level = stringLevelMap.get(levelName);
                List<MapState> mapStates = level.createMapStates();
                if (mapStates.size() == 1) {
                    solveProblem(levelName, mapStates.get(0), resultWriter);
                } else {
                    for (int problemIndex = 0; problemIndex < mapStates.size(); problemIndex++) {
                        solveProblem(levelName + " " + problemIndex, mapStates.get(problemIndex), resultWriter);
                    }
                }
            }
        }
    }

    private static void solveProblem(@NotNull String levelName,
                                     @NotNull MapState mapState,
                                     @NotNull BufferedWriter resultWriter) throws IOException {
        System.out.println(DATE_FORMAT.format(new Date()) +" Calculating problem [" + levelName + "]");
        resultWriter.write(levelName);
        resultWriter.newLine();
        long startTime = System.nanoTime();

        LinkedList<MapState> states = new LinkedList<>();
        states.add(mapState);
        boolean solution = false;
        while ((!solution) && (!states.isEmpty())) {
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if (solution) {
                long stopTime = System.nanoTime();
                System.out.println("Solved in " + LocalTime.MIN.plusNanos((stopTime - startTime)).toString());
                String[] solutionAsStringArray = nextCandidate.printableGrid();
                for (String solutionLine : solutionAsStringArray) {
                    resultWriter.write(solutionLine);
                    resultWriter.newLine();
                }
            }
        }
        if (!solution) {
            long stopTime = System.nanoTime();
            System.out.println("Failed to solve in "+ LocalTime.MIN.plusNanos((stopTime - startTime)).toString());
        }
        resultWriter.newLine();
        resultWriter.flush();
    }

}
