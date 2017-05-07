package net.archiloque.cosmic_express;

class TrainElement {

    final byte trainElementStatus;

    /**
     * A TrainElementContent
     */
    final byte content;

    final int coordinates;

    final boolean taintedByGreenMonster;

    TrainElement(byte trainElementStatus,
                 byte content,
                 int coordinates,
                 boolean taintedByGreenMonster) {
        this.trainElementStatus = trainElementStatus;
        this.content = content;
        this.coordinates = coordinates;
        this.taintedByGreenMonster = taintedByGreenMonster;
    }
}
