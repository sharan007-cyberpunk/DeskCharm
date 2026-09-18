package com.sharan.deskcharm.interaction;

import javafx.scene.input.MouseEvent;

public final class MouseTracker {
    private double x;
    private double y;

    public void update(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    public double x() { return x; }
    public double y() { return y; }
}
