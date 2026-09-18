package com.sharan.deskcharm.physics;

/**
 * Immutable tunable parameters for a {@link RopeSimulation}.
 * Instances are typically produced from user settings (see the settings package
 * in a later stage) so the physics engine itself stays free of I/O concerns.
 */
public final class RopeConfiguration {

    /** Number of rope segments; there are segmentCount + 1 nodes. */
    private final int segmentCount;

    /** Rest length of a single segment, in pixels. */
    private final double segmentLength;

    /** Downward acceleration applied every fixed step, in pixels/second^2. */
    private final Vector2 gravity;

    /** Velocity damping factor applied per step, in the range (0, 1]. 1.0 = no damping. */
    private final double damping;

    /** Number of Gauss-Seidel style constraint relaxation passes per physics step. */
    private final int constraintIterations;

    /** Maximum allowed stretch as a multiplier of segmentLength (e.g. 1.15 = 15% stretch). */
    private final double maxStretchFactor;

    /** Squared speed (px/s)^2 below which the rope is considered eligible to sleep. */
    private final double sleepVelocityThreshold;

    /** Consecutive fixed steps below the sleep threshold required before sleeping. */
    private final int sleepStepsRequired;

    public RopeConfiguration(int segmentCount,
                              double segmentLength,
                              Vector2 gravity,
                              double damping,
                              int constraintIterations,
                              double maxStretchFactor,
                              double sleepVelocityThreshold,
                              int sleepStepsRequired) {
        if (segmentCount < 1) {
            throw new IllegalArgumentException("segmentCount must be >= 1");
        }
        if (segmentLength <= 0) {
            throw new IllegalArgumentException("segmentLength must be > 0");
        }
        if (damping <= 0 || damping > 1.0) {
            throw new IllegalArgumentException("damping must be in (0, 1]");
        }
        if (constraintIterations < 1) {
            throw new IllegalArgumentException("constraintIterations must be >= 1");
        }
        if (maxStretchFactor < 1.0) {
            throw new IllegalArgumentException("maxStretchFactor must be >= 1.0");
        }
        this.segmentCount = segmentCount;
        this.segmentLength = segmentLength;
        this.gravity = gravity;
        this.damping = damping;
        this.constraintIterations = constraintIterations;
        this.maxStretchFactor = maxStretchFactor;
        this.sleepVelocityThreshold = sleepVelocityThreshold;
        this.sleepStepsRequired = sleepStepsRequired;
    }

    /** Sensible default configuration matching the 20-segment / 21-node spec. */
    public static RopeConfiguration defaults() {
        return new RopeConfiguration(
                20,
                9.0,
                new Vector2(0.0, 620.0),
                0.985,
                8,
                1.12,
                4.0,
                20
        );
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public int getNodeCount() {
        return segmentCount + 1;
    }

    public double getSegmentLength() {
        return segmentLength;
    }

    public double getRestLength() {
        return segmentCount * segmentLength;
    }

    public Vector2 getGravity() {
        return gravity;
    }

    public double getDamping() {
        return damping;
    }

    public int getConstraintIterations() {
        return constraintIterations;
    }

    public double getMaxStretchFactor() {
        return maxStretchFactor;
    }

    public double getSleepVelocityThreshold() {
        return sleepVelocityThreshold;
    }

    public int getSleepStepsRequired() {
        return sleepStepsRequired;
    }

    /** Returns a copy of this configuration with a different segment length (used by rope-length setting). */
    public RopeConfiguration withSegmentLength(double newSegmentLength) {
        return new RopeConfiguration(segmentCount, newSegmentLength, gravity, damping,
                constraintIterations, maxStretchFactor, sleepVelocityThreshold, sleepStepsRequired);
    }

    /** Returns a copy of this configuration with a different gravity magnitude (Y component only). */
    public RopeConfiguration withGravityY(double gravityY) {
        return new RopeConfiguration(segmentCount, segmentLength, new Vector2(gravity.getX(), gravityY), damping,
                constraintIterations, maxStretchFactor, sleepVelocityThreshold, sleepStepsRequired);
    }

    /** Returns a copy of this configuration with a different damping factor. */
    public RopeConfiguration withDamping(double newDamping) {
        return new RopeConfiguration(segmentCount, segmentLength, gravity, newDamping,
                constraintIterations, maxStretchFactor, sleepVelocityThreshold, sleepStepsRequired);
    }
}
