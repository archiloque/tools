package net.archiloque.roofbot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LinkedList<E> {

    private ListElement<E> current;

    LinkedList() {
    }

    void add(@Nullable E element) {
        if (element != null) {
            current = new ListElement<E>(element, current);
        }
    }

    @Nullable E pop() {
        if (current != null) {
            E currentElement = current.element;
            current = current.previous;
            return currentElement;
        } else {
            return null;
        }
    }

    boolean isEmpty() {
        return current == null;
    }

    private final static class ListElement<E> {

        private final @NotNull E element;

        private final @Nullable ListElement<E> previous;

        ListElement(@NotNull E element, @Nullable ListElement<E> previous) {
            this.element = element;
            this.previous = previous;
        }
    }

}
