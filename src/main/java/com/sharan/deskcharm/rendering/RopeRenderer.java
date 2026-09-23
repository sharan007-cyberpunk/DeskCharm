package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.physics.RopeNode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import java.util.List;

/** Material-aware premium cord. It changes with the selected charm; physics remain untouched. */
public final class RopeRenderer {
    public void draw(GraphicsContext g, List<RopeNode> nodes, Charm charm) {
        if (nodes.size() < 2) return;
        CharmVisualTheme t = CharmVisualTheme.forType(charm.type());
        var first = nodes.get(0).position();
        var last = nodes.get(nodes.size()-1).position();
        LinearGradient material = new LinearGradient(first.x(),first.y(),last.x(),last.y(),false,CycleMethod.NO_CYCLE,
                new Stop(0,t.ropeHighlight()),new Stop(.18,t.ropeBase()),new Stop(.72,t.ropeBase()),new Stop(1,t.ropeShadow()));
        g.save();
        g.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        g.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        g.setEffect(new DropShadow(5,0,2,Color.rgb(0,0,0,.25)));
        g.setLineWidth(5.4);g.setStroke(Color.color(t.ropeShadow().getRed(),t.ropeShadow().getGreen(),t.ropeShadow().getBlue(),.55));stroke(g,nodes);
        g.setEffect(null);
        g.setLineWidth(3.35);
        g.setStroke(material);
        stroke(g,nodes);

        // New rope design: a subtle braided/twisted weave over the material core.
        // Short alternating diagonal bands give the cord a handcrafted rope texture
        // without changing the underlying physics or its total length.
        g.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        for (int i = 0; i < nodes.size() - 1; i++) {
            var a = nodes.get(i).position();
            var b = nodes.get(i + 1).position();
            double dx = b.x() - a.x();
            double dy = b.y() - a.y();
            double len = Math.hypot(dx, dy);
            if (len < 1e-6) continue;
            double nx = -dy / len;
            double ny = dx / len;
            double cx = (a.x() + b.x()) * 0.5;
            double cy = (a.y() + b.y()) * 0.5;
            double half = Math.min(4.5, len * 0.28);
            double along = Math.min(3.2, len * 0.22);
            double sign = (i & 1) == 0 ? 1 : -1;
            double ox = nx * sign * along;
            double oy = ny * sign * along;
            g.setLineWidth(1.05);
            g.setStroke(Color.color(t.ropeHighlight().getRed(), t.ropeHighlight().getGreen(),
                    t.ropeHighlight().getBlue(), 0.72));
            g.strokeLine(cx - nx * half + ox, cy - ny * half + oy,
                    cx + nx * half - ox, cy + ny * half - oy);
        }

        // Refined cap / bail at the anchor.
        g.setFill(t.metal());g.fillOval(first.x()-4.6,first.y()-4.6,9.2,9.2);
        g.setFill(t.metalHighlight());g.fillOval(first.x()-2.0,first.y()-2.7,3.0,3.0);
        g.restore();
    }
    public void draw(GraphicsContext g,List<RopeNode> nodes){draw(g,nodes,new Charm("default","Default",com.sharan.deskcharm.charm.CharmType.CUSTOM,30));}
    private void stroke(GraphicsContext g,List<RopeNode> nodes){for(int i=0;i<nodes.size()-1;i++){var a=nodes.get(i).position();var b=nodes.get(i+1).position();g.strokeLine(a.x(),a.y(),b.x(),b.y());}}
}
