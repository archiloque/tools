package net.archiloque.cosmic_express;

final class Direction {

    private Direction(){
    }

    final static int UP = 0;
    final static int DOWN = 1;
    final static int LEFT = 2;
    final static int RIGHT = 3;

    static char toChar(int direction) {
        if(direction == UP) {
            return '↑';
        } else if(direction == DOWN) {
            return '↓';
        } else if(direction == LEFT) {
            return '←';
        } else if(direction == RIGHT) {
            return '→';
        } else {
            throw new RuntimeException("Unknwown direction " + direction);
        }
    }

}
