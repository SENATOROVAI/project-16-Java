package ru.student.vehiclecli.model;

/**
 * Vehicle coordinates.
 */
public final class Coordinates {
    private final Double x;
    private final long y;

    public Coordinates(Double x, long y) {
        validateX(x);
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    private static void validateX(Double x) {
        if (x == null) {
            throw new IllegalArgumentException("Coordinates.x must not be null");
        }
        if (x <= -501) {
            throw new IllegalArgumentException("Coordinates.x must be greater than -501");
        }
    }
}


