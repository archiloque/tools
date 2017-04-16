package net.archiloque.cosmic_express;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class TrainElement {

    final int trainElementStatus;

    final int content;

    final boolean head;

    final int coordinates;

    TrainElement(boolean head,
                 int trainElementStatus,
                 int content,
                 int coordinates) {
        this.head = head;
        this.trainElementStatus = trainElementStatus;
        this.content = content;
        this.coordinates = coordinates;
    }
}
