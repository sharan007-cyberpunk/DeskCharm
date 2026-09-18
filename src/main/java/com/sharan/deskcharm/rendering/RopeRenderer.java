package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.physics.RopeNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

public final class RopeRenderer {
    public void draw(GraphicsContext g, List<RopeNode> nodes) {
        if (nodes.size() < 2) return;
        g.setLineWidth(3.2);
        g.setStroke(Color.rgb(55, 55, 65, 0.90));
        for (int i = 0; i < nodes.size() - 1; i++) {
            var a = nodes.get(i).position();
            var b = nodes.get(i + 1).position();
            g.strokeLine(a.x(), a.y(), b.x(), b.y());
        }
        g.setLineWidth(1.0);
        g.setStroke(Color.rgb(220, 220, 230, 0.45));
        for (int i = 0; i < nodes.size() - 1; i++) {
            var a = nodes.get(i).position();
            var b = nodes.get(i + 1).position();
            g.strokeLine(a.x() - 0.6, a.y(), b.x() - 0.6, b.y());
        }
    }
}
