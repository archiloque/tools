package net.archiloque.roofbot;

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
import java.util.ArrayList;
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
            printWithTimestamp(problemName, "Init level");
            List<MapState> mapStates = new ArrayList<>();
            mapStates.add(level.createMapState());
            if (mapStates.size() == 1) {
                solveProblem(problemName, mapStates.get(0), resultWriter);
            }
        }
    }

    private static void solveProblem(@NotNull String problemName,
                                     @NotNull MapState mapState,
                                     @NotNull BufferedWriter resultWriter) throws IOException {
        printWithTimestamp(problemName, "Calculating problem");
        long startTime = System.nanoTime();

        LinkedList<MapState> states = new LinkedList<>();
        states.add(mapState);
        boolean solution = false;
        while ((!solution) && (!states.isEmpty())) {
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if (solution) {
                long stopTime = System.nanoTime();
                printWithTimestamp(problemName, "Solved in " + LocalTime.MIN.plusNanos((stopTime - startTime)).toString());
                String[] solutionAsStringArray = nextCandidate.printableGrid();
                for (String solutionLine : solutionAsStringArray) {
                    resultWriter.write(solutionLine);
                    resultWriter.newLine();
                }
                resultWriter.newLine();
                String[] pathAsStringArray = nextCandidate.printablePath();
                for (String solutionLine : pathAsStringArray) {
                    resultWriter.write(solutionLine);
                    resultWriter.newLine();
                }
            }
        }
        if (!solution) {
            long stopTime = System.nanoTime();
            printWithTimestamp(problemName, "Failed to solve in " + LocalTime.MIN.plusNanos((stopTime - startTime)).toString());
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
