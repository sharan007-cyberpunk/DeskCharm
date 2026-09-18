package com.sharan.deskcharm.physics;

/**
 * A single point mass in the Verlet-integrated rope chain.
 * Holds current and previous position (implicit velocity), mass and pinned state.
 */
public class RopeNode {

    private Vector2 position;
    private Vector2 previousPosition;
    private double mass;
    private boolean pinned;

    public RopeNode(Vector2 position, double mass, boolean pinned) {
        this.position = position;
        this.previousPosition = position;
        this.mass = mass;
        this.pinned = pinned;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Vector2 previousPosition) {
        this.previousPosition = previousPosition;
    }

    /**
     * Moves the node to a new position while preserving its previous position,
     * which is required to correctly seed Verlet velocity (e.g. after a hard teleport).
     */
    public void teleportTo(Vector2 newPosition) {
        this.position = newPosition;
        this.previousPosition = newPosition;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    /**
     * Implicit velocity derived from the difference between current and previous position,
     * as used internally by Verlet integration.
     */
    public Vector2 getImplicitVelocity() {
        return position.subtract(previousPosition);
    }

    /**
     * Directly sets the implicit velocity by repositioning the previous-position sample.
     * Used when applying a throw impulse at the moment of release.
     */
    public void setImplicitVelocity(Vector2 velocity) {
        this.previousPosition = this.position.subtract(velocity);
    }
}
