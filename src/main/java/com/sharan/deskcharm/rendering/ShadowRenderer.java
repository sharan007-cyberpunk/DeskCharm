package com.sharan.deskcharm.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/** Small helper for drawing soft, fading elliptical shadows beneath scene elements. */
public class ShadowRenderer {

    public void renderEllipseShadow(GraphicsContext gc, double centerX, double centerY, double width, double height) {
        RadialGradient gradient = new RadialGradient(
                0, 0, centerX, centerY, Math.max(width, height),
                false, null,
                new Stop(0.0, Color.rgb(0, 0, 0, 0.28)),
                new Stop(1.0, Color.rgb(0, 0, 0, 0.0))
        );
        gc.save();
        gc.setFill(gradient);
        gc.fillOval(centerX - width / 2.0, centerY - height / 2.0, width, height);
        gc.restore();
    }
}
