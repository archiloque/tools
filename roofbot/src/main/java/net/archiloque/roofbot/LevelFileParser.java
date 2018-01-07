package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class LevelFileParser {

    @NotNull Level parseFile(@NotNull Path levelFile) throws IOException {
        LevelParser levelParser = new LevelParser();
        try (BufferedReader reader = Files.newBufferedReader(levelFile)) {
            String currentLine = reader.readLine();
            List<String> elements = new ArrayList<>();
            while (!currentLine.isEmpty()) {
                elements.add(currentLine);
                currentLine = reader.readLine();
            }
            currentLine = reader.readLine();
            List<String> strengths = new ArrayList<>();
            while (currentLine != null) {
                strengths.add(currentLine);
                currentLine = reader.readLine();
            }
            return levelParser.readLevel(
                    elements.toArray(new String[elements.size()]),
                    strengths.toArray(new String[strengths.size()])
            );
        }
    }
}
