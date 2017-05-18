package net.archiloque.cosmic_express;

final class TrainElement {

    /**
     * A TrainElementContent
     */
    final byte content;

    /**
     * -1 : out of board
     */
    final int line;

    /**
     * -1 : out of board
     */
    final int column;

    final boolean taintedByGreenMonster;

    TrainElement(byte content,
                 int line,
                 int column,
                 boolean taintedByGreenMonster) {
        this.content = content;
        this.line = line;
        this.column = column;
        this.taintedByGreenMonster = taintedByGreenMonster;
    }
}
