package com.sharan.deskcharm.physics;

/**
 * Immutable 2D vector used throughout the physics simulation.
 * Kept free of any JavaFX dependency so the physics package can be
 * unit tested and reused independently of the rendering layer.
 */
public final class Vector2 {

    public static final Vector2 ZERO = new Vector2(0.0, 0.0);

    private final double x;
    private final double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 scale(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    public double dot(Vector2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distanceTo(Vector2 other) {
        return this.subtract(other).length();
    }

    public Vector2 normalize() {
        double len = length();
        if (len < 1.0e-9) {
            return ZERO;
        }
        return new Vector2(x / len, y / len);
    }

    /**
     * Linear interpolation between this vector and {@code target} by factor {@code t} (0..1).
     */
    public Vector2 lerp(Vector2 target, double t) {
        return new Vector2(
                this.x + (target.x - this.x) * t,
                this.y + (target.y - this.y) * t
        );
    }

    public Vector2 clampLength(double maxLength) {
        double len = length();
        if (len <= maxLength || len < 1.0e-9) {
            return this;
        }
        return this.scale(maxLength / len);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2 other)) return false;
        return Double.compare(x, other.x) == 0 && Double.compare(y, other.y) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) * 31 + Double.hashCode(y);
    }

    @Override
    public String toString() {
        return String.format("Vector2(%.4f, %.4f)", x, y);
    }
}
