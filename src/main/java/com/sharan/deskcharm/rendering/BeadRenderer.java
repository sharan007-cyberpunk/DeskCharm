package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.physics.Bead;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.List;

/** Renders decorative beads as small shaded circles sampled along the rope curve. */
public class BeadRenderer {

    public void render(GraphicsContext gc, List<Bead> beads) {
        for (Bead bead : beads) {
            double x = bead.getRenderPosition().getX();
            double y = bead.getRenderPosition().getY();
            double r = bead.getRadius();

            RadialGradient gradient = new RadialGradient(
                    0, 0, x - r * 0.3, y - r * 0.3, r * 1.4,
                    false, null,
                    new Stop(0.0, Color.rgb(255, 225, 170)),
                    new Stop(1.0, Color.rgb(180, 130, 60))
            );

            gc.save();
            gc.setFill(gradient);
            gc.fillOval(x - r, y - r, r * 2, r * 2);
            gc.setStroke(Color.rgb(90, 60, 20, 0.6));
            gc.setLineWidth(1.0);
            gc.strokeOval(x - r, y - r, r * 2, r * 2);
            gc.restore();
        }
    }
}
