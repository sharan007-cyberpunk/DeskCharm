package com.sharan.deskcharm.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verlet-integrated rope simulation: a fixed top anchor connected by
 * {@code segmentCount} distance-constrained segments to a free bottom node
 * (the charm attachment point). Supports mouse dragging of the bottom node
 * and computes a release velocity to "throw" the charm.
 *
 * This class intentionally has zero JavaFX dependencies so it can be unit
 * tested headlessly and reused by any rendering technology.
 */
public class RopeSimulation {

    private final RopeConfiguration config;
    private final List<RopeNode> nodes;
    private final SimulationClock clock = new SimulationClock();

    private Vector2 anchor;

    private boolean dragging = false;
    private Vector2 lastDragPosition;
    private Vector2 previousDragPosition;

    private boolean sleeping = false;
    private int consecutiveSlowSteps = 0;

    public RopeSimulation(RopeConfiguration config, Vector2 anchor) {
        this.config = config;
        this.anchor = anchor;
        this.nodes = new ArrayList<>(config.getNodeCount());
        initializeNodes();
    }

    private void initializeNodes() {
        nodes.clear();
        for (int i = 0; i < config.getNodeCount(); i++) {
            Vector2 position = anchor.add(new Vector2(0.0, i * config.getSegmentLength()));
            boolean pinned = (i == 0);
            nodes.add(new RopeNode(position, 1.0, pinned));
        }
    }

    /**
     * Advances the simulation by {@code deltaTime} seconds of wall-clock time,
     * internally subdividing into fixed {@link SimulationClock#FIXED_TIMESTEP}
     * steps for numerical stability.
     */
    public void update(double deltaTime) {
        if (sleeping && !dragging) {
            // Still allow the anchor to move (e.g. window reposition) without waking fully,
            // but skip the expensive integration/constraint work while at rest.
            return;
        }
        clock.accumulate(deltaTime);
        while (clock.consumeStep()) {
            step(SimulationClock.FIXED_TIMESTEP);
        }
    }

    private void step(double dt) {
        applyAnchor();
        integrate(dt);
        satisfyConstraints();
        limitStretch();
        if (dragging) {
            // The dragged node is driven directly by the mouse, not by integration.
            RopeNode bottom = nodes.get(nodes.size() - 1);
            bottom.teleportTo(lastDragPosition);
        }
        updateSleepState();
    }

    private void applyAnchor() {
        RopeNode top = nodes.get(0);
        top.teleportTo(anchor);
    }

    private void integrate(double dt) {
        Vector2 gravityStep = config.getGravity().scale(dt * dt);
        for (int i = 1; i < nodes.size(); i++) {
            RopeNode node = nodes.get(i);
            if (node.isPinned()) {
                continue;
            }
            if (dragging && i == nodes.size() - 1) {
                // Driven directly in step(); skip normal integration this frame.
                continue;
            }
            Vector2 velocity = node.getImplicitVelocity().scale(config.getDamping());
            Vector2 newPosition = node.getPosition().add(velocity).add(gravityStep);
            node.setPreviousPosition(node.getPosition());
            node.setPosition(newPosition);
        }
    }

    private void satisfyConstraints() {
        double restLength = config.getSegmentLength();
        for (int iteration = 0; iteration < config.getConstraintIterations(); iteration++) {
            for (int i = 0; i < nodes.size() - 1; i++) {
                RopeNode a = nodes.get(i);
                RopeNode b = nodes.get(i + 1);

                Vector2 delta = b.getPosition().subtract(a.getPosition());
                double currentLength = delta.length();
                if (currentLength < 1.0e-9) {
                    continue;
                }
                double difference = (currentLength - restLength) / currentLength;

                boolean bFixed = dragging && (i + 1 == nodes.size() - 1);

                double aMoveFactor;
                double bMoveFactor;
                if (a.isPinned() && bFixed) {
                    continue;
                } else if (a.isPinned()) {
                    aMoveFactor = 0.0;
                    bMoveFactor = 1.0;
                } else if (bFixed) {
                    aMoveFactor = 1.0;
                    bMoveFactor = 0.0;
                } else {
                    aMoveFactor = 0.5;
                    bMoveFactor = 0.5;
                }

                Vector2 correction = delta.scale(difference);
                if (aMoveFactor > 0) {
                    a.setPosition(a.getPosition().add(correction.scale(aMoveFactor)));
                }
                if (bMoveFactor > 0) {
                    b.setPosition(b.getPosition().subtract(correction.scale(bMoveFactor)));
                }
            }
        }
    }

    /**
     * Hard clamp pass preventing any single segment from exceeding
     * {@code restLength * maxStretchFactor}, which matters most during fast
     * dragging/throwing where a single relaxation pass may not fully converge.
     */
    private void limitStretch() {
        double restLength = config.getSegmentLength();
        double maxLength = restLength * config.getMaxStretchFactor();
        for (int i = 0; i < nodes.size() - 1; i++) {
            RopeNode a = nodes.get(i);
            RopeNode b = nodes.get(i + 1);
            Vector2 delta = b.getPosition().subtract(a.getPosition());
            double currentLength = delta.length();
            if (currentLength <= maxLength || currentLength < 1.0e-9) {
                continue;
            }
            Vector2 direction = delta.scale(1.0 / currentLength);
            Vector2 excess = direction.scale(currentLength - maxLength);
            if (a.isPinned() && (dragging && i + 1 == nodes.size() - 1)) {
                continue;
            } else if (a.isPinned()) {
                b.setPosition(b.getPosition().subtract(excess));
            } else if (dragging && i + 1 == nodes.size() - 1) {
                a.setPosition(a.getPosition().add(excess));
            } else {
                a.setPosition(a.getPosition().add(excess.scale(0.5)));
                b.setPosition(b.getPosition().subtract(excess.scale(0.5)));
            }
        }
    }

    private void updateSleepState() {
        double maxSpeedSquared = 0.0;
        for (RopeNode node : nodes) {
            if (node.isPinned()) {
                continue;
            }
            double speedSquared = node.getImplicitVelocity().lengthSquared()
                    / (SimulationClock.FIXED_TIMESTEP * SimulationClock.FIXED_TIMESTEP);
            maxSpeedSquared = Math.max(maxSpeedSquared, speedSquared);
        }
        double thresholdSquared = config.getSleepVelocityThreshold() * config.getSleepVelocityThreshold();
        if (maxSpeedSquared < thresholdSquared) {
            consecutiveSlowSteps++;
            if (consecutiveSlowSteps >= config.getSleepStepsRequired()) {
                sleeping = true;
            }
        } else {
            consecutiveSlowSteps = 0;
            sleeping = false;
        }
    }

    public void setAnchor(Vector2 newAnchor) {
        this.anchor = newAnchor;
        wakeUp();
    }

    public Vector2 getAnchor() {
        return anchor;
    }

    public void beginDrag(Vector2 mousePosition) {
        dragging = true;
        lastDragPosition = mousePosition;
        previousDragPosition = mousePosition;
        wakeUp();
    }

    public void updateDrag(Vector2 mousePosition) {
        if (!dragging) {
            return;
        }
        previousDragPosition = lastDragPosition;
        lastDragPosition = mousePosition;
    }

    /**
     * Ends the drag and imparts a throw velocity to the bottom node, derived
     * from the most recent mouse movement recorded via {@link #updateDrag(Vector2)}.
     */
    public void endDrag() {
        if (!dragging) {
            return;
        }
        dragging = false;
        RopeNode bottom = nodes.get(nodes.size() - 1);
        Vector2 releaseVelocity = Vector2.ZERO;
        if (previousDragPosition != null && lastDragPosition != null) {
            releaseVelocity = lastDragPosition.subtract(previousDragPosition);
        }
        bottom.teleportTo(lastDragPosition);
        bottom.setImplicitVelocity(releaseVelocity);
        wakeUp();
    }

    public boolean isDragging() {
        return dragging;
    }

    public List<RopeNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public Vector2 getCharmPosition() {
        return nodes.get(nodes.size() - 1).getPosition();
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public void wakeUp() {
        sleeping = false;
        consecutiveSlowSteps = 0;
    }

    public RopeConfiguration getConfiguration() {
        return config;
    }
}
