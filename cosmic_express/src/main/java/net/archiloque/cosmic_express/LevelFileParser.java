package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LevelFileParser {

    enum ReadingStatus {
        HEADER,
        LOOKING_FOR_TITLE,
        LOOKING_FOR_TRAIN_SIZE,
        LOOKING_FOR_CONTENT
    }

    LevelFileParser() {

    }

    @NotNull Map<String, Level> parseFile(Path levelFiles) throws IOException {
        LevelParser levelParser = new LevelParser();
        Map<String, Level> result = new HashMap<>();
        try(BufferedReader reader = Files.newBufferedReader(levelFiles)) {
            ReadingStatus currentStatus = ReadingStatus.HEADER;
            String currentLine = reader.readLine();
            String currentLevelName = null;
            int currentTrainSize = -1;
            List<String> currentContent = new ArrayList<>();

            while (currentLine != null) {
                if (currentStatus == ReadingStatus.HEADER) {
                    if (currentLine.startsWith("#")) {
                        currentLine = reader.readLine();
                        continue;
                    } else {
                        currentStatus = ReadingStatus.LOOKING_FOR_TITLE;
                    }
                }
                if (currentStatus == ReadingStatus.LOOKING_FOR_CONTENT) {
                    if (currentLine.length() == 0) {
                        currentLine = reader.readLine();
                        continue;
                    } else if (currentLine.startsWith("[")) {
                        result.put(
                                currentLevelName,
                                levelParser.readLevel(currentContent.toArray(new String[currentContent.size()]), currentTrainSize)
                        );

                        currentStatus = ReadingStatus.LOOKING_FOR_TITLE;
                        currentContent = new ArrayList<>();
                    } else {
                        currentContent.add(currentLine);
                        currentLine = reader.readLine();
                        continue;
                    }
                }
                if (currentStatus == ReadingStatus.LOOKING_FOR_TITLE) {
                    if (currentLine.length() == 0) {
                        currentLine = reader.readLine();
                        continue;
                    } else if (currentLine.startsWith("[") && currentLine.endsWith("]")) {
                        currentLevelName = currentLine.substring(1, currentLine.length() - 1);
                        currentStatus = ReadingStatus.LOOKING_FOR_TRAIN_SIZE;
                        currentLine = reader.readLine();
                        continue;
                    } else {
                        throw new RuntimeException("Unexpected line [" + currentLine + "]");
                    }
                }
                if (currentStatus == ReadingStatus.LOOKING_FOR_TRAIN_SIZE) {
                    if (currentLine.length() == 0) {
                        currentLine = reader.readLine();
                        continue;
                    } else if (currentLine.startsWith("train_size = ")) {
                        currentTrainSize = Integer.parseInt(currentLine.substring(13, currentLine.length()));
                        currentStatus = ReadingStatus.LOOKING_FOR_CONTENT;
                        currentLine = reader.readLine();
                        continue;
                    } else {
                        throw new RuntimeException("Unexpected line [" + currentLine + "]");
                    }
                }
            }
            if(! currentContent.isEmpty()) {
                result.put(
                        currentLevelName,
                        levelParser.readLevel(currentContent.toArray(new String[currentContent.size()]), currentTrainSize)
                );
            }

        }

        return result;
    }
}
