package com.sharan.deskcharm.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RopeSimulation {
    private RopeConfiguration config;
    private final List<RopeNode> nodes = new ArrayList<>();
    private Vector2 anchor = Vector2.ZERO;
    private boolean held;
    private Vector2 dragTarget = Vector2.ZERO;
    private double sleepTime;

    public RopeSimulation(RopeConfiguration config) {
        this.config = config;
        reset(Vector2.ZERO);
    }

    public void reset(Vector2 newAnchor) {
        anchor = newAnchor;
        nodes.clear();
        for (int i = 0; i <= config.segments(); i++) {
            nodes.add(new RopeNode(
                    new Vector2(anchor.x(), anchor.y() + i * config.segmentLength()),
                    i == 0));
        }
        held = false;
        sleepTime = 0;
    }

    public void setAnchor(Vector2 newAnchor) {
        anchor = newAnchor;
        nodes.get(0).setPosition(newAnchor);
        nodes.get(0).setPreviousPosition(newAnchor);
    }

    /** Rebuilds the rope with a new total length while preserving the segment count. */
    public void setRopeLength(double totalLength) {
        if (totalLength <= 0) throw new IllegalArgumentException("totalLength must be positive");
        double segmentLength = totalLength / config.segments();
        config = new RopeConfiguration(config.segments(), segmentLength, config.gravity(),
                config.damping(), config.constraintIterations(), config.maxStretch());
        reset(anchor);
    }

    public double getRopeLength() {
        return config.segments() * config.segmentLength();
    }

    public List<RopeNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public Vector2 getCharmPosition() {
        return nodes.get(nodes.size() - 1).position();
    }

    public boolean isHeld() { return held; }
    public boolean isSleeping() { return sleepTime > 0.5; }

    public void wakeUp() { sleepTime = 0; }

    public void beginDrag(Vector2 target) {
        held = true;
        dragTarget = target;
        wakeUp();
    }

    public void updateDrag(Vector2 target) {
        dragTarget = target;
        wakeUp();
    }

    public void endDrag() {
        held = false;
        wakeUp();
    }

    public void step(double dt) {
        RopeNode first = nodes.get(0);
        first.setPosition(anchor);
        first.setPreviousPosition(anchor);

        double maxReach = config.segments() * config.segmentLength() * config.maxStretch();
        Vector2 desired = dragTarget.subtract(anchor).clampLength(maxReach).add(anchor);

        for (int i = 1; i < nodes.size(); i++) {
            RopeNode n = nodes.get(i);
            Vector2 velocity = n.position().subtract(n.previousPosition()).multiply(config.damping());
            Vector2 next = n.position().add(velocity).add(new Vector2(0, config.gravity() * dt * dt));
            n.setPreviousPosition(n.position());
            n.setPosition(next);
        }

        if (held) {
            RopeNode end = nodes.get(nodes.size() - 1);
            Vector2 delta = desired.subtract(end.position());
            double maxMove = 2200.0 * dt;
            Vector2 move = delta.clampLength(maxMove);
            Vector2 old = end.position();
            end.setPosition(old.add(move));
            end.setPreviousPosition(old);
        }

        for (int iteration = 0; iteration < config.constraintIterations(); iteration++) {
            first.setPosition(anchor);
            for (int i = 0; i < nodes.size() - 1; i++) {
                RopeNode a = nodes.get(i);
                RopeNode b = nodes.get(i + 1);
                Vector2 delta = b.position().subtract(a.position());
                double distance = delta.length();
                if (distance < 1e-9) continue;

                double rest = config.segmentLength();
                double max = rest * config.maxStretch();
                double correction;

                if (distance > max) {
                    correction = distance - max;
                } else {
                    correction = distance - rest;
                }

                Vector2 correctionVector = delta.normalized().multiply(correction);
                if (a.pinned()) {
                    b.setPosition(b.position().subtract(correctionVector));
                } else if (b.pinned()) {
                    a.setPosition(a.position().add(correctionVector));
                } else {
                    a.setPosition(a.position().add(correctionVector.multiply(0.5)));
                    b.setPosition(b.position().subtract(correctionVector.multiply(0.5)));
                }
            }
        }

        // Final one-sided stretch-ceiling pass. The iterative solver can move an
        // earlier node again when solving a later segment, so a single forward
        // pass guarantees that no segment ends the step beyond maxStretch.
        first.setPosition(anchor);
        for (int i = 0; i < nodes.size() - 1; i++) {
            RopeNode a = nodes.get(i);
            RopeNode b = nodes.get(i + 1);
            Vector2 delta = b.position().subtract(a.position());
            double distance = delta.length();
            double max = config.segmentLength() * config.maxStretch();
            if (distance > max && distance > 1e-9) {
                b.setPosition(a.position().add(delta.normalized().multiply(max)));
            }
        }

        double maxSpeed = 0;
        for (RopeNode n : nodes) {
            maxSpeed = Math.max(maxSpeed, n.position().distance(n.previousPosition()) / dt);
        }
        if (!held && maxSpeed < 2.0) sleepTime += dt;
        else sleepTime = 0;
    }
}
