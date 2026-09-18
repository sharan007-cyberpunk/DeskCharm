package com.sharan.deskcharm.physics;

public final class RopeNode {
    private Vector2 position;
    private Vector2 previousPosition;
    private final boolean pinned;

    public RopeNode(Vector2 position, boolean pinned) {
        this.position = position;
        this.previousPosition = position;
        this.pinned = pinned;
    }

    public Vector2 position() { return position; }
    public Vector2 previousPosition() { return previousPosition; }
    public boolean pinned() { return pinned; }

    public void setPosition(Vector2 p) { position = p; }
    public void setPreviousPosition(Vector2 p) { previousPosition = p; }
}
