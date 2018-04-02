package net.archiloque.roofbot;

final class Coordinates {

    final byte line;

    final byte column;

    Coordinates(byte line, byte column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Coordinates that = (Coordinates) o;
            return (line == that.line) && (column == that.column);
        }
    }

    @Override
    public int hashCode() {
        return 100 * line + column;
    }

    @Override
    public String toString() {
        return "(" + line + "," + column + ")";
    }
}
