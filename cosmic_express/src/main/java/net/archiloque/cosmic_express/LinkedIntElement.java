package net.archiloque.cosmic_express;

import org.jetbrains.annotations.Nullable;

/**
 * An element to build a linked list.
 */
class LinkedIntElement {

    final int element;

    final @Nullable LinkedIntElement previous;

    LinkedIntElement(int element, LinkedIntElement previous) {
        this.element = element;
        this.previous = previous;
    }
}
