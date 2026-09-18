package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.physics.RopeNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;

import java.util.List;

/**
 * Renders the rope as a smooth curve through its Verlet nodes. This class
 * only reads physics state (the node list) and never mutates it, keeping
 * rendering and simulation cleanly separated.
 */
public class RopeRenderer {

    private final double baseThickness;

    public RopeRenderer(double baseThickness) {
        this.baseThickness = baseThickness;
    }

    public void render(GraphicsContext gc, List<RopeNode> nodes) {
        if (nodes.size() < 2) {
            return;
        }

        LinearGradient shading = new LinearGradient(
                0, 0, 1, 0, true, null,
                new Stop(0.0, Color.rgb(90, 80, 70)),
                new Stop(0.5, Color.rgb(150, 135, 115)),
                new Stop(1.0, Color.rgb(90, 80, 70))
        );

        gc.save();
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(shading);
        gc.setLineWidth(baseThickness);

        gc.beginPath();
        gc.moveTo(nodes.get(0).getPosition().getX(), nodes.get(0).getPosition().getY());

        // Draw a smooth curve using quadratic segments through midpoints,
        // rather than a rough polyline directly between every node.
        for (int i = 1; i < nodes.size() - 1; i++) {
            double currentX = nodes.get(i).getPosition().getX();
            double currentY = nodes.get(i).getPosition().getY();
            double nextX = nodes.get(i + 1).getPosition().getX();
            double nextY = nodes.get(i + 1).getPosition().getY();
            double midX = (currentX + nextX) / 2.0;
            double midY = (currentY + nextY) / 2.0;
            gc.quadraticCurveTo(currentX, currentY, midX, midY);
        }
        RopeNode last = nodes.get(nodes.size() - 1);
        gc.lineTo(last.getPosition().getX(), last.getPosition().getY());
        gc.stroke();

        // Thin highlight down the center for a rounded, lit appearance.
        gc.setStroke(Color.rgb(210, 200, 185, 0.55));
        gc.setLineWidth(Math.max(1.0, baseThickness * 0.28));
        gc.stroke();

        gc.restore();
    }
}
