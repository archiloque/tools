package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An element to build a linked list.
 */
final class CoordinatesLinkedItem {

    private final int coordinates;

    private final @Nullable CoordinatesLinkedItem previous;

    CoordinatesLinkedItem(int coordinates, @Nullable CoordinatesLinkedItem previous) {
        this.coordinates = coordinates;
        this.previous = previous;
    }

    @NotNull List<Coordinates> getAsList(@NotNull Level level) {
        List<Coordinates> trainPathList = new ArrayList<>();

        CoordinatesLinkedItem trainPath = this;
        while (trainPath != null) {
            byte line = (byte) (trainPath.coordinates / level.width);
            byte column = (byte) (trainPath.coordinates % level.width);
            trainPathList.add( new Coordinates(line, column));
            trainPath = trainPath.previous;
        }
        Collections.reverse(trainPathList);
        return trainPathList;
    }

    public String toString() {
        return Integer.toString(coordinates);
    }
}