package com.sharan.deskcharm.physics;

public record Vector2(double x, double y) {
    public static final Vector2 ZERO = new Vector2(0, 0);

    public Vector2 add(Vector2 o) { return new Vector2(x + o.x, y + o.y); }
    public Vector2 subtract(Vector2 o) { return new Vector2(x - o.x, y - o.y); }
    public Vector2 multiply(double s) { return new Vector2(x * s, y * s); }
    public double length() { return Math.hypot(x, y); }
    public double distance(Vector2 o) { return subtract(o).length(); }
    public Vector2 normalized() {
        double l = length();
        return l < 1e-9 ? ZERO : multiply(1.0 / l);
    }
    public Vector2 clampLength(double max) {
        double l = length();
        return l > max ? normalized().multiply(max) : this;
    }
}
