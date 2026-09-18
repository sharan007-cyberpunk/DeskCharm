package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmType;
import com.sharan.deskcharm.physics.Vector2;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders whichever {@link Charm} is currently selected, at the rope's bottom
 * node. Every built-in {@link CharmType} is original geometry (no Hangly
 * artwork); a {@code CUSTOM_IMAGE} charm instead draws the user's imported,
 * already-processed image (see {@code CharmImageProcessor}).
 */
public class CharmRenderer {

    private static final Color DEFAULT_PRIMARY = Color.rgb(230, 110, 150);
    private static final Color DEFAULT_SECONDARY = Color.rgb(160, 50, 90);

    private final ShadowRenderer shadowRenderer = new ShadowRenderer();
    private final Map<String, Image> imageCache = new HashMap<>();

    /** Renders the given charm at {@code position}; {@code hovered} slightly enlarges it. */
    public void render(GraphicsContext gc, Vector2 position, Charm charm, boolean hovered) {
        double size = charm.getSize();
        double x = position.getX();
        double y = position.getY();

        shadowRenderer.renderEllipseShadow(gc, x, y + size * 0.75, size * 0.9, size * 0.28);

        gc.save();
        gc.translate(x, y);
        if (hovered) {
            gc.scale(1.06, 1.06);
        }

        if (charm.getType() == CharmType.CUSTOM_IMAGE) {
            renderCustomImage(gc, charm, size);
        } else {
            renderBuiltInShape(gc, charm, size);
        }

        // Small connector between the rope's last node and the top of the charm.
        gc.setStroke(Color.rgb(150, 135, 115));
        gc.setLineWidth(2.5);
        gc.strokeLine(0, -size / 2.0, 0, -size / 2.0 - 6);

        gc.restore();
    }

    /** Backward-compatible overload for callers that don't yet have a full Charm (renders a default diamond). */
    public void render(GraphicsContext gc, Vector2 position, double size, boolean hovered) {
        render(gc, position, com.sharan.deskcharm.charm.Charm.builtIn(
                "preview", "Preview", CharmType.ROUNDED_DIAMOND, size, null, null, 1.0), hovered);
    }

    private void renderCustomImage(GraphicsContext gc, Charm charm, double size) {
        String path = charm.getImagePath().orElse(null);
        if (path == null) {
            renderBuiltInShape(gc, charm, size);
            return;
        }
        Image image = imageCache.computeIfAbsent(path, p -> {
            try {
                return new Image(new java.io.File(p).toURI().toString());
            } catch (Exception e) {
                return null;
            }
        });
        if (image == null || image.isError()) {
            renderBuiltInShape(gc, charm, size);
            return;
        }
        gc.drawImage(image, -size / 2.0, -size / 2.0, size, size);
    }

    private void renderBuiltInShape(GraphicsContext gc, Charm charm, double size) {
        double half = size / 2.0;
        Color primary = charm.getPrimaryColorHex().map(Color::web).orElse(DEFAULT_PRIMARY);
        Color secondary = charm.getSecondaryColorHex().map(Color::web).orElse(DEFAULT_SECONDARY);

        Paint fill = new RadialGradient(
                0, 0, -half * 0.3, -half * 0.3, size,
                false, null,
                new Stop(0.0, primary.interpolate(Color.WHITE, 0.35)),
                new Stop(0.55, primary),
                new Stop(1.0, secondary)
        );

        gc.beginPath();
        switch (charm.getType()) {
            case STAR -> buildStarPath(gc, half);
            case CIRCLE_PENDANT -> gc.appendSVGPath(circleSvg(half));
            case CRESCENT -> buildCrescentPath(gc, half);
            case LEAF -> buildLeafPath(gc, half);
            case ROUNDED_DIAMOND, CUSTOM_IMAGE -> buildDiamondPath(gc, half);
        }
        gc.setFill(fill);
        gc.fill();
        gc.setStroke(Color.rgb(255, 255, 255, 0.65));
        gc.setLineWidth(1.5);
        gc.stroke();
    }

    private void buildDiamondPath(GraphicsContext gc, double half) {
        gc.moveTo(0, -half);
        gc.lineTo(half * 0.78, 0);
        gc.lineTo(0, half);
        gc.lineTo(-half * 0.78, 0);
        gc.closePath();
    }

    private void buildStarPath(GraphicsContext gc, double half) {
        int points = 5;
        double outerRadius = half;
        double innerRadius = half * 0.42;
        for (int i = 0; i < points * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = Math.PI / points * i - Math.PI / 2;
            double px = radius * Math.cos(angle);
            double py = radius * Math.sin(angle);
            if (i == 0) {
                gc.moveTo(px, py);
            } else {
                gc.lineTo(px, py);
            }
        }
        gc.closePath();
    }

    private String circleSvg(double half) {
        return String.format(
                "M %f,0 A %f,%f 0 1,0 %f,0.0001 A %f,%f 0 1,0 %f,0 Z",
                -half, half, half, half, half, half, -half
        );
    }

    private void buildCrescentPath(GraphicsContext gc, double half) {
        // Outer circle minus an offset inner circle, using the canvas's even-odd-like
        // subtractive trick via two arcs traced in opposite winding order.
        gc.arc(0, 0, half, half, 90, 270);
        gc.arc(half * 0.55, 0, half * 0.8, half * 0.8, 270, -270);
        gc.closePath();
    }

    private void buildLeafPath(GraphicsContext gc, double half) {
        gc.moveTo(0, -half);
        gc.bezierCurveTo(half, -half * 0.4, half, half * 0.6, 0, half);
        gc.bezierCurveTo(-half, half * 0.6, -half, -half * 0.4, 0, -half);
        gc.closePath();
    }
}
