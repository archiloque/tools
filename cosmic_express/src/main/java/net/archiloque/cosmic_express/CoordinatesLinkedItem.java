package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An element to build a linked list.
 */
final class CoordinatesLinkedItem {

    final int line;

    final int column;

    private final @Nullable CoordinatesLinkedItem previous;

    CoordinatesLinkedItem(int line, int column, @Nullable CoordinatesLinkedItem previous) {
        this.line = line;
        this.column = column;
        this.previous = previous;
    }

    @NotNull List<Coordinates> getAsList(@NotNull Level level) {
        List<Coordinates> trainPathList = new ArrayList<>();

        CoordinatesLinkedItem trainPath = this;
        while (trainPath != null) {
            trainPathList.add(new Coordinates(trainPath.line, trainPath.column));
            trainPath = trainPath.previous;
        }
        trainPathList.add(new Coordinates(level.entryLine, level.entryColumn));
        Collections.reverse(trainPathList);
        return trainPathList;
    }

    public String toString() {
        return " (" + line + ", " + column + ")";
    }
}
