package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public final class CharmRenderer {
    public void draw(GraphicsContext g, Charm charm, double x, double y, Image custom, boolean shadow) {
        double r = charm.radius();

        if (shadow) {
            g.setFill(Color.rgb(0, 0, 0, 0.18));
            g.fillOval(x - r + 4, y - r + 6, r * 2, r * 2);
        }

        if (custom != null) {
            g.drawImage(custom, x - r, y - r, r * 2, r * 2);
            return;
        }

        g.setStroke(Color.rgb(35, 35, 45, 0.9));
        g.setLineWidth(1.5);

        switch (charm.type()) {
            case STAR -> {
                g.setFill(Color.GOLD);
                double[] xs = new double[10], ys = new double[10];
                for (int i = 0; i < 10; i++) {
                    double a = -Math.PI / 2 + i * Math.PI / 5;
                    double rr = i % 2 == 0 ? r : r * 0.43;
                    xs[i] = x + Math.cos(a) * rr;
                    ys[i] = y + Math.sin(a) * rr;
                }
                g.fillPolygon(xs, ys, 10);
                g.strokePolygon(xs, ys, 10);
            }
            case MOON -> {
                g.setFill(Color.LIGHTSKYBLUE);
                g.fillOval(x-r, y-r, 2*r, 2*r);
                g.setFill(Color.rgb(30, 30, 40, 1));
                g.fillOval(x-r*0.55, y-r*1.0, 2*r, 2*r);
            }
            case PLANET -> {
                g.setStroke(Color.LIGHTSTEELBLUE);
                g.setLineWidth(5);
                g.strokeOval(x-r*1.2, y-r*0.45, r*2.4, r*0.9);
                g.setFill(Color.MEDIUMPURPLE);
                g.fillOval(x-r*0.72, y-r*0.72, r*1.44, r*1.44);
                g.setStroke(Color.DARKSLATEBLUE);
                g.setLineWidth(1.5);
                g.strokeOval(x-r*0.72, y-r*0.72, r*1.44, r*1.44);
            }
            case DIAMOND -> {
                g.setFill(Color.ORANGE);
                double[] xs = {x, x+r, x, x-r};
                double[] ys = {y-r, y, y+r, y};
                g.fillPolygon(xs, ys, 4);
                g.strokePolygon(xs, ys, 4);
            }
            case LEAF -> {
                g.setFill(Color.LIGHTGREEN);
                g.fillOval(x-r*0.55, y-r, r*1.1, r*2);
                g.setStroke(Color.DARKGREEN);
                g.strokeLine(x-r*0.5, y+r*0.65, x+r*0.55, y-r*0.65);
            }
            default -> {
                g.setFill(Color.LIGHTGRAY);
                g.fillOval(x-r, y-r, 2*r, 2*r);
            }
        }
    }
}
