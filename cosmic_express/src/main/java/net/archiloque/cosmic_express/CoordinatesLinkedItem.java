package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An element to build a linked list.
 */
class CoordinatesLinkedItem {

    final int element;

    final @Nullable CoordinatesLinkedItem previous;

    CoordinatesLinkedItem(int element, @Nullable CoordinatesLinkedItem previous) {
        this.element = element;
        this.previous = previous;
    }

    @NotNull List<Integer> getAsArray(@NotNull Level level) {
        List<Integer> trainPathList = new ArrayList<>();

        CoordinatesLinkedItem trainPath = this;
        while (trainPath != null) {
            trainPathList.add(trainPath.element);
            trainPath = trainPath.previous;
        }
        trainPathList.add(level.entry);
        Collections.reverse(trainPathList);
        return trainPathList;
    }

    public String toString() {
        return element + " (" + (element >> 16) + ", " + (element & 65535) + ")";
    }
}
