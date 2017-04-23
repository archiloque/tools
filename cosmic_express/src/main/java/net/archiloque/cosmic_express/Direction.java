package net.archiloque.cosmic_express;

final class Direction {

    private Direction() {
    }

    final static byte UP = 0;
    final static byte DOWN = 1;
    final static byte LEFT = 2;
    final static byte RIGHT = 3;

    static char toChar(int direction) {
        if (direction == UP) {
            return '↑';
        } else if (direction == DOWN) {
            return '↓';
        } else if (direction == LEFT) {
            return '←';
        } else if (direction == RIGHT) {
            return '→';
        } else {
            throw new RuntimeException("Unknwown direction " + direction);
        }
    }

    static char toCharEntry(int direction) {
        if (direction == UP) {
            return '⇧';
        } else if (direction == DOWN) {
            return '⇩';
        } else if (direction == LEFT) {
            return '⇦';
        } else if (direction == RIGHT) {
            return '⇨';
        } else {
            throw new RuntimeException("Unknwown direction " + direction);
        }
    }

}
