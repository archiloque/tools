package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class App {

    private static LevelParser LEVEL_PARSER = new LevelParser();

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
                List<MapState> mapStates = level.createMapState();
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

    private static void solveProblem(@NotNull String levelName, @NotNull MapState mapState, @NotNull BufferedWriter resultWriter) throws IOException {
        System.out.println("Calculating problem [" + levelName + "]");
        resultWriter.write(levelName);
        resultWriter.newLine();
        long startTime = System.currentTimeMillis();

        LinkedList<MapState> states = new LinkedList<>();
        states.add(mapState);
        byte[] solution = null;
        while ((solution == null) && (!states.isEmpty())) {
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if (solution != null) {
                long stopTime = System.currentTimeMillis();
                System.out.println("Solved in " + (((float) (stopTime - startTime)) / 1000));
                char[][] solutionAsChar = printableSolution(nextCandidate, solution);
                for (char[] chars : solutionAsChar) {
                    resultWriter.write(chars);
                    resultWriter.newLine();
                }
                resultWriter.newLine();
            }
        }

    }

    private static @NotNull Coordinates coordinatesFromDirection(@NotNull Coordinates coordinates, int direction) {
        switch (direction) {
            case Direction.UP:
                return new Coordinates(coordinates.line - 1, coordinates.column);
            case Direction.DOWN:
                return new Coordinates(coordinates.line + 1, coordinates.column);
            case Direction.LEFT:
                return new Coordinates(coordinates.line, coordinates.column - 1);
            case Direction.RIGHT:
                return new Coordinates(coordinates.line, coordinates.column + 1);
            default:
                throw new RuntimeException("Unknown direction [" + direction + "]");
        }
    }

    private static @NotNull char[][] printableSolution(@NotNull MapState solution, @NotNull byte[] grid) {
        char[][] result = new char[solution.level.height][];
        for (int lineIndex = 0; lineIndex < solution.level.height; lineIndex++) {
            char[] lineChar = new char[solution.level.width];
            for (int columnIndex = 0; columnIndex < solution.level.width; columnIndex++) {
                byte element = grid[(lineIndex * solution.level.width) + columnIndex];
                lineChar[columnIndex] = LEVEL_PARSER.elementsToChars.get(element);
            }
            result[lineIndex] = lineChar;
        }
        Coordinates currentPoint = new Coordinates(solution.level.entry >> 16, solution.level.entry & 65535);
        result[solution.level.entry >> 16][solution.level.entry & 65535] = LEVEL_PARSER.elementsToChars.get(MapElement.ENTRY_INDEX);
        for (int i = 0; i < solution.previousTrainPath.length - 1; i++) {
            int from = solution.previousTrainPath[i];
            int fromLine = from >> 16;
            int fromColumn = from & 65535;
            int to = solution.previousTrainPath[i + 1];
            int toLine = to >> 16;
            int toColumn = to & 65535;
            int direction;
            if (toLine == (fromLine + 1)) {
                direction = Direction.DOWN;
            } else if (toLine == (fromLine - 1)) {
                direction = Direction.UP;
            } else if (toColumn == (fromColumn - 1)) {
                direction = Direction.LEFT;
            } else {
                direction = Direction.RIGHT;
            }
            result[fromLine][fromColumn] = Direction.toChar(direction);
            currentPoint = coordinatesFromDirection(currentPoint, direction);
        }
        result[solution.exitCoordinates >> 16][solution.exitCoordinates & 65535] = LEVEL_PARSER.elementsToChars.get(MapElement.EXIT_INDEX);
        return result;
    }
}
