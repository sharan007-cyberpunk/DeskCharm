package com.sharan.deskcharm.interaction;

import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.physics.Vector2;

public final class DragController {
    private final RopeSimulation simulation;
    private final double radius;

    public DragController(RopeSimulation simulation, double radius) {
        this.simulation = simulation;
        this.radius = radius;
    }

    public boolean hit(Vector2 point) {
        return simulation.getCharmPosition().distance(point) <= radius;
    }

    public void press(Vector2 point) {
        if (hit(point)) simulation.beginDrag(point);
    }

    public void drag(Vector2 point) {
        if (simulation.isHeld()) simulation.updateDrag(point);
    }

    public void release() {
        simulation.endDrag();
    }
}
