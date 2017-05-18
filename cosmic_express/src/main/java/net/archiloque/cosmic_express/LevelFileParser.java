package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class LevelFileParser {

    LevelFileParser() {

    }

    @NotNull Level parseFile(@NotNull Path levelFile) throws IOException {
        LevelParser levelParser = new LevelParser();
        try (BufferedReader reader = Files.newBufferedReader(levelFile)) {
            int currentTrainSize = -1;
            String currentLine = reader.readLine();
            if (! currentLine.startsWith("train_size = ")) {
                throw new RuntimeException("Unexpected line [" + currentLine + "]");
            }
            currentTrainSize = Integer.parseInt(currentLine.substring(13, currentLine.length()));
            List<String> currentContent = new ArrayList<>();
            currentLine = reader.readLine();
            while (currentLine != null) {
                if(currentLine.length() != 0) {
                    currentContent.add(currentLine);
                }
                currentLine = reader.readLine();
            }
            return levelParser.readLevel(currentContent.toArray(new String[currentContent.size()]), currentTrainSize);
        }
    }
}
