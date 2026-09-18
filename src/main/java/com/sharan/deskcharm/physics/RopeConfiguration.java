package com.sharan.deskcharm.physics;

public record RopeConfiguration(
        int segments,
        double segmentLength,
        double gravity,
        double damping,
        int constraintIterations,
        double maxStretch) {

    public RopeConfiguration {
        if (segments < 1) throw new IllegalArgumentException("segments must be positive");
        if (segmentLength <= 0) throw new IllegalArgumentException("segmentLength must be positive");
        if (gravity < 0) throw new IllegalArgumentException("gravity cannot be negative");
        if (damping <= 0 || damping > 1) throw new IllegalArgumentException("damping must be in (0,1]");
        if (constraintIterations < 1) throw new IllegalArgumentException("iterations must be positive");
        if (maxStretch < 1) throw new IllegalArgumentException("maxStretch must be >= 1");
    }
}
