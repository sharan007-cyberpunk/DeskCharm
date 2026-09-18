package com.sharan.deskcharm.interaction;

import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.physics.Vector2;

import java.util.function.Consumer;

/**
 * Coordinates hover/grab/drag/release/throw interaction against a
 * RopeSimulation. This class contains the interaction *logic* only;
 * actual JavaFX mouse event wiring happens in OverlayWindow, which
 * calls the methods below in response to real mouse events.
 */
public final class DragController {

    private static final double DEFAULT_HIT_RADIUS = 40.0;

    private final RopeSimulation simulation;
    private final MouseTracker mouseTracker = new MouseTracker();
    private final double hitRadius;

    private InteractionState state = InteractionState.IDLE;
    private Consumer<InteractionState> onStateChanged;

    public DragController(RopeSimulation simulation) {
        this(simulation, DEFAULT_HIT_RADIUS);
    }

    public DragController(RopeSimulation simulation, double hitRadius) {
        this.simulation = simulation;
        this.hitRadius = hitRadius;
    }

    public void setOnStateChanged(Consumer<InteractionState> callback) {
        this.onStateChanged = callback;
    }

    public InteractionState getState() {
        return state;
    }

    public double getHitRadius() {
        return hitRadius;
    }

    /** Call on every mouse-move event, even when not grabbed, to update hover state. */
    public void onMouseMoved(double x, double y) {
        mouseTracker.update(x, y);
        if (state == InteractionState.GRABBED) {
            simulation.updateDrag(new Vector2(x, y));
            return;
        }
        boolean near = mouseTracker.isNear(simulation.getCharmPosition(), hitRadius);
        setState(near ? InteractionState.HOVERING : InteractionState.IDLE);
    }

    public void onMousePressed(double x, double y) {
        mouseTracker.update(x, y);
        if (mouseTracker.isNear(simulation.getCharmPosition(), hitRadius)) {
            simulation.beginDrag(new Vector2(x, y));
            setState(InteractionState.GRABBED);
        }
    }

    public void onMouseDragged(double x, double y) {
        mouseTracker.update(x, y);
        if (state == InteractionState.GRABBED) {
            simulation.updateDrag(new Vector2(x, y));
        }
    }

    public void onMouseReleased(double x, double y) {
        mouseTracker.update(x, y);
        if (state == InteractionState.GRABBED) {
            simulation.endDrag();
            boolean near = mouseTracker.isNear(simulation.getCharmPosition(), hitRadius);
            setState(near ? InteractionState.HOVERING : InteractionState.IDLE);
        }
    }

    /** Returns true if the point should be treated as "on the charm" for click-through purposes. */
    public boolean hitTest(double x, double y) {
        return new Vector2(x, y).distanceTo(simulation.getCharmPosition()) <= hitRadius
                || state == InteractionState.GRABBED;
    }

    private void setState(InteractionState newState) {
        if (newState != state) {
            state = newState;
            if (onStateChanged != null) {
                onStateChanged.accept(newState);
            }
        }
    }
}
