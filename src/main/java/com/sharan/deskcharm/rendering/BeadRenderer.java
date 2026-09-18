package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.physics.RopeNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

public final class BeadRenderer {
    public void draw(GraphicsContext g, List<RopeNode> nodes) {
        int count = Math.min(5, Math.max(1, nodes.size() / 5));
        for (int k = 1; k <= count; k++) {
            int index = Math.min(nodes.size() - 2, Math.max(1, k * nodes.size() / (count + 1)));
            var p = nodes.get(index).position();
            g.setFill(Color.rgb(245, 205, 100, 0.95));
            g.fillOval(p.x() - 5, p.y() - 5, 10, 10);
            g.setFill(Color.rgb(255, 255, 255, 0.55));
            g.fillOval(p.x() - 2.5, p.y() - 3, 3, 3);
        }
    }
}
