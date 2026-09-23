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
        int screenIndex,
        double charmScale,
        String dangleText) {

    public static AppSettings defaults() {
        return new AppSettings("evil-eye", 20, 15, 900, 0.995, 12, 1.025,
                true, true, false, true, 0, 1.0, "");
    }

    /** Copy of these settings with a different charm selected (used when picking a charm or
     *  creating a Name Dangle) so callers don't have to restate every unrelated field. */
    public AppSettings withSelectedCharm(String id) {
        return new AppSettings(id, segments, segmentLength, gravity, damping, constraintIterations,
                maxStretch, beads, shadows, sound, animation, screenIndex, charmScale, dangleText);
    }

    /** Copy of these settings with a different charm size multiplier (the size-control slider). */
    /** Copy with a different total rope length in pixels. Segment count is preserved. */
    public AppSettings withRopeLength(double totalLength) {
        double safeLength = Math.max(5, totalLength);
        return new AppSettings(selectedCharm, segments, safeLength / Math.max(1, segments), gravity, damping, constraintIterations,
                maxStretch, beads, shadows, sound, animation, screenIndex, charmScale, dangleText);
    }

    /** Total rope length in pixels represented by the current segment count/length. */
    public double ropeLength() {
        return segments * segmentLength;
    }

    public AppSettings withCharmScale(double scale) {
        return new AppSettings(selectedCharm, segments, segmentLength, gravity, damping, constraintIterations,
                maxStretch, beads, shadows, sound, animation, screenIndex, scale, dangleText);
    }

    /** Copy of these settings recording the last text typed into Name Dangle, so it's remembered
     *  (and the dangle can be recreated) across app restarts. */
    public AppSettings withDangleText(String text) {
        return new AppSettings(selectedCharm, segments, segmentLength, gravity, damping, constraintIterations,
                maxStretch, beads, shadows, sound, animation, screenIndex, charmScale, text);
    }
}
