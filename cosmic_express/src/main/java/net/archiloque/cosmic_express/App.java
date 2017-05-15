package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public class App {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("We need one parameter for the problem(s) file(s)");
        }

        if (!new File("solutions").exists()) {
            new File("solutions").mkdir();
        }

        String problemGlob = args[0];
        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher("glob:levels/" + problemGlob + "*.txt");

        Files.walkFileTree(Paths.get("levels/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (matcher.matches(file)) {
                    try {
                        processFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

    }

    private static void processFile(Path problemsFile) throws IOException {
        String fileName = problemsFile.getFileName().toString();
        System.out.println("Reading problem from [" + problemsFile.toAbsolutePath() + "]");
        LevelFileParser levelFileParser = new LevelFileParser();
        Level level = levelFileParser.parseFile(problemsFile);
        String problemName = fileName.substring(0, fileName.length() - 4);
        try (BufferedWriter resultWriter = Files.newBufferedWriter(Paths.get("solutions/" + problemName + ".txt"))) {
            printWithTimestamp(fileName, "Init level");
            List<MapState> mapStates = level.createMapStates();
            if (mapStates.size() == 1) {
                solveProblem(problemName, "", mapStates.get(0), resultWriter);
            } else {
                for (int problemIndex = 0; problemIndex < mapStates.size(); problemIndex++) {
                    solveProblem(problemName, "" + problemIndex, mapStates.get(problemIndex), resultWriter);
                }
            }
        }
    }

    private static void solveProblem(@NotNull String problemName,
                                     @NotNull String levelName,
                                     @NotNull MapState mapState,
                                     @NotNull BufferedWriter resultWriter) throws IOException {
        printWithTimestamp(problemName, "[" + levelName + "] Calculating problem");
        if (!levelName.isEmpty()) {
            resultWriter.write(levelName);
            resultWriter.newLine();
        }
        long startTime = System.nanoTime();

        LinkedList<MapState> states = new LinkedList<>();
        states.add(mapState);
        boolean solution = false;
        while ((!solution) && (!states.isEmpty())) {
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if (solution) {
                long stopTime = System.nanoTime();
                printWithTimestamp(problemName, levelName.isEmpty() ? "" : ("[" + levelName + "] ") + "Solved in " + LocalTime.MIN.plusNanos((stopTime - startTime)).toString());
                String[] solutionAsStringArray = nextCandidate.printableGrid();
                for (String solutionLine : solutionAsStringArray) {
                    resultWriter.write(solutionLine);
                    resultWriter.newLine();
                }
            }
        }
        if (!solution) {
            long stopTime = System.nanoTime();
            printWithTimestamp(problemName, levelName.isEmpty() ? "" : ("[" + levelName + "] ") + "Failed to solve in " + LocalTime.MIN.plusNanos((stopTime - startTime)).toString());
            resultWriter.write("FAILED");
            resultWriter.newLine();
        }
        resultWriter.newLine();
        resultWriter.flush();
    }

    private static void printWithTimestamp(@NotNull String problemsSet, @NotNull String message) {
        System.out.println(problemsSet + " " + DATE_FORMAT.format(new Date()) + " " + message);
    }

}
