package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
                List<MapState> mapStates = level.createMapStates();
                if (mapStates.size() == 1) {
                    solveProblem(levelName, level, mapStates.get(0), resultWriter);
                } else {
                    for (int problemIndex = 0; problemIndex < mapStates.size(); problemIndex++) {
                        solveProblem(levelName + " " + problemIndex, level, mapStates.get(problemIndex), resultWriter);
                    }
                }
            }
        }
    }

    private static void solveProblem(@NotNull String levelName,
                                     @NotNull Level level,
                                     @NotNull MapState mapState,
                                     @NotNull BufferedWriter resultWriter) throws IOException {
        System.out.println("Calculating problem [" + levelName + "]");
        resultWriter.write(levelName);
        resultWriter.newLine();
        long startTime = System.currentTimeMillis();

        LinkedList<MapState> states = new LinkedList<>();
        states.add(mapState);
        boolean[] solution = null;
        while ((solution == null) && (!states.isEmpty())) {
            MapState nextCandidate = states.pop();
            solution = nextCandidate.processState(states);
            if (solution != null) {
                long stopTime = System.currentTimeMillis();
                System.out.println("Solved in " + (((float) (stopTime - startTime)) / 1000));
                char[][] solutionAsChar = printableSolution(nextCandidate, level, solution);
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

    private static @NotNull char[][] printableSolution(@NotNull MapState solution, @NotNull Level level, @NotNull boolean[] grid) {
        char[][] result = new char[level.height][];
        for (int lineIndex = 0; lineIndex < level.height; lineIndex++) {
            char[] lineChar = new char[level.width];
            for (int columnIndex = 0; columnIndex < level.width; columnIndex++) {
                byte element = level.grid[(lineIndex * level.width) + columnIndex];
                Character character = LEVEL_PARSER.elementsToChars.get(element);
                if(character == null) {
                    throw new RuntimeException("Unknown element [" + element + "]");
                }
                lineChar[columnIndex] = character;
            }
            result[lineIndex] = lineChar;
        }
        Coordinates currentPoint = new Coordinates(level.entry >> 16, level.entry & 65535);
        result[level.entry >> 16][level.entry & 65535] = LEVEL_PARSER.elementsToChars.get(MapElement.ENTRY_INDEX);

        int[] trainPath = getTrainPath(solution);
        for (int i = 0; i < trainPath.length - 1; i++) {
            int from = trainPath[i];
            int fromLine = from >> 16;
            int fromColumn = from & 65535;
            int to = trainPath[i + 1];
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

    private static int[] getTrainPath(@NotNull MapState solution) {
        List<Integer> trainPathList = new ArrayList<>();

        LinkedIntElement trainPath = solution.previousTrainPath;
        while (trainPath != null) {
            trainPathList.add(trainPath.element);
            trainPath = trainPath.previous;
        }
        Collections.reverse(trainPathList);
        return Level.listToPrimitiveIntArray(trainPathList);
    }
}
