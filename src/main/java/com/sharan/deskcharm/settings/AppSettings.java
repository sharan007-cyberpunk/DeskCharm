package com.sharan.deskcharm.settings;

public record AppSettings(
        String selectedCharm,
        int segments,
        double segmentLength,
        double gravity,
        double damping,
        int constraintIterations,
        double maxStretch,
        boolean beads,
        boolean shadows,
        boolean sound,
        boolean animation,
        int screenIndex) {

    public static AppSettings defaults() {
        return new AppSettings("star", 20, 15, 900, 0.995, 12, 1.025,
                true, true, false, true, 0);
    }
}
