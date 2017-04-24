package net.archiloque.cosmic_express;

class TrainElement {

    final byte trainElementStatus;

    /**
     * A TrainElementContent
     */
    final byte content;

    final int coordinates;

    TrainElement(byte trainElementStatus,
                 byte content,
                 int coordinates) {
        this.trainElementStatus = trainElementStatus;
        this.content = content;
        this.coordinates = coordinates;
    }
}
