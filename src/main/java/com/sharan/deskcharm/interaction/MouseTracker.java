package com.sharan.deskcharm.interaction;

import com.sharan.deskcharm.physics.Vector2;

/**
 * Tracks the most recent mouse position (in overlay-window-local coordinates)
 * and answers proximity queries used to decide hover and grab eligibility.
 */
public final class MouseTracker {

    private Vector2 currentPosition = Vector2.ZERO;

    public void update(double x, double y) {
        this.currentPosition = new Vector2(x, y);
    }

    public Vector2 getPosition() {
        return currentPosition;
    }

    /**
     * @return true if the mouse is within {@code hitRadius} pixels of
     *         {@code targetPosition}, used for charm-only interaction (the
     *         overlay otherwise lets clicks pass through to the desktop).
     */
    public boolean isNear(Vector2 targetPosition, double hitRadius) {
        return currentPosition.distanceTo(targetPosition) <= hitRadius;
    }
}
