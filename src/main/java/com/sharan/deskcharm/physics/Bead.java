package com.sharan.deskcharm.physics;

/**
 * A small decorative bead that rides along the rope at a fixed fractional
 * position (0.0 = anchor, 1.0 = charm). Its rendered position is derived by
 * sampling the rope's node chain rather than being independently simulated,
 * so beads can never visually separate from the rope.
 */
public class Bead {
    private final double restFraction;
    private final double radius;
    private Vector2 renderPosition = Vector2.ZERO;

    public Bead(double restFraction, double radius) {
        if (restFraction < 0.0 || restFraction > 1.0) {
            throw new IllegalArgumentException("restFraction must be within [0, 1]");
        }
        this.restFraction = restFraction;
        this.radius = radius;
    }

    public double getRestFraction() { return restFraction; }
    public double getRadius() { return radius; }
    public Vector2 getRenderPosition() { return renderPosition; }
    public void setRenderPosition(Vector2 renderPosition) { this.renderPosition = renderPosition; }

    public void sampleFrom(java.util.List<RopeNode> nodes) {
        if (nodes.size() < 2) {
            renderPosition = nodes.isEmpty() ? Vector2.ZERO : nodes.get(0).position();
            return;
        }

        double scaledIndex = restFraction * (nodes.size() - 1);
        int lowerIndex = (int) Math.floor(scaledIndex);
        int upperIndex = Math.min(lowerIndex + 1, nodes.size() - 1);
        double t = scaledIndex - lowerIndex;

        Vector2 lower = nodes.get(lowerIndex).position();
        Vector2 upper = nodes.get(upperIndex).position();

        renderPosition = new Vector2(
                lower.x() + (upper.x() - lower.x()) * t,
                lower.y() + (upper.y() - lower.y()) * t
        );
    }
}
