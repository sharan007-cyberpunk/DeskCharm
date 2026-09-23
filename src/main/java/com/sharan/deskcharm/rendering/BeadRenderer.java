package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.physics.RopeNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

/** Bead knots along the cord. Color/material follows the charm's theme so each collection reads as one set. */
public final class BeadRenderer {
    public void draw(GraphicsContext g, List<RopeNode> nodes, Charm charm) {
        CharmVisualTheme t = CharmVisualTheme.forType(charm.type());
        int count = Math.min(5, Math.max(1, nodes.size() / 5));
        for (int k = 1; k <= count; k++) {
            int index = Math.min(nodes.size() - 2, Math.max(1, k * nodes.size() / (count + 1)));
            var p = nodes.get(index).position();
            g.setFill(t.metal());
            g.fillOval(p.x() - 5, p.y() - 5, 10, 10);
            g.setFill(t.metalHighlight());
            g.fillOval(p.x() - 2.5, p.y() - 3, 3, 3);
        }
    }

    /** Backwards-compatible default (plain gold), kept for any caller without charm context. */
    public void draw(GraphicsContext g, List<RopeNode> nodes) {
        draw(g, nodes, new Charm("default", "Default", com.sharan.deskcharm.charm.CharmType.CUSTOM, 30));
    }
}
