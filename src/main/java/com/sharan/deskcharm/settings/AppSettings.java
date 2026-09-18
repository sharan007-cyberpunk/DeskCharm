package com.sharan.deskcharm.settings;

/**
 * Plain, mutable data holder for all user-configurable settings. Instances
 * are created by {@link SettingsStore} (never as a global singleton — the
 * store owns the single active instance and hands references to whoever
 * needs them), validated on load, and written back to JSON on save.
 */
public class AppSettings {

    public static final String DEFAULT_SELECTED_CHARM_ID = "star";
    public static final double DEFAULT_ROPE_LENGTH = 180.0;
    public static final double DEFAULT_ROPE_THICKNESS = 4.0;
    public static final double DEFAULT_GRAVITY = 620.0;
    public static final double DEFAULT_DAMPING = 0.985;
    public static final boolean DEFAULT_BEADS_VISIBLE = true;
    public static final boolean DEFAULT_SHADOWS_VISIBLE = true;
    public static final boolean DEFAULT_SOUND_ENABLED = false;
    public static final boolean DEFAULT_ANIMATION_ENABLED = true;
    public static final boolean DEFAULT_LAUNCH_AT_STARTUP = false;
    public static final int DEFAULT_SELECTED_MONITOR_INDEX = 0;
    public static final double DEFAULT_OVERLAY_OFFSET_X = 0.0;
    public static final double DEFAULT_OVERLAY_OFFSET_Y = 0.0;

    private String selectedCharmId = DEFAULT_SELECTED_CHARM_ID;
    private double ropeLength = DEFAULT_ROPE_LENGTH;
    private double ropeThickness = DEFAULT_ROPE_THICKNESS;
    private double gravity = DEFAULT_GRAVITY;
    private double damping = DEFAULT_DAMPING;
    private boolean beadsVisible = DEFAULT_BEADS_VISIBLE;
    private boolean shadowsVisible = DEFAULT_SHADOWS_VISIBLE;
    private boolean soundEnabled = DEFAULT_SOUND_ENABLED;
    private boolean animationEnabled = DEFAULT_ANIMATION_ENABLED;
    private boolean launchAtStartup = DEFAULT_LAUNCH_AT_STARTUP;
    private int selectedMonitorIndex = DEFAULT_SELECTED_MONITOR_INDEX;
    private double overlayOffsetX = DEFAULT_OVERLAY_OFFSET_X;
    private double overlayOffsetY = DEFAULT_OVERLAY_OFFSET_Y;

    public AppSettings() {
        // Jackson requires a no-arg constructor; defaults above are used as-is.
    }

    /** Returns a brand-new settings object populated entirely with defaults. */
    public static AppSettings defaults() {
        return new AppSettings();
    }

    /**
     * Clamps every numeric field into a sane range and falls back to defaults
     * for anything invalid (e.g. from hand-edited or partially corrupt JSON),
     * so a bad settings file can never crash the app or produce a broken rope.
     */
    public void validateAndRepair() {
        if (selectedCharmId == null || selectedCharmId.isBlank()) {
            selectedCharmId = DEFAULT_SELECTED_CHARM_ID;
        }
        ropeLength = clamp(ropeLength, 60.0, 500.0, DEFAULT_ROPE_LENGTH);
        ropeThickness = clamp(ropeThickness, 1.0, 12.0, DEFAULT_ROPE_THICKNESS);
        gravity = clamp(gravity, 50.0, 3000.0, DEFAULT_GRAVITY);
        damping = clamp(damping, 0.80, 1.0, DEFAULT_DAMPING);
        selectedMonitorIndex = Math.max(0, selectedMonitorIndex);
    }

    private double clamp(double value, double min, double max, double fallback) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    public String getSelectedCharmId() {
        return selectedCharmId;
    }

    public void setSelectedCharmId(String selectedCharmId) {
        this.selectedCharmId = selectedCharmId;
    }

    public double getRopeLength() {
        return ropeLength;
    }

    public void setRopeLength(double ropeLength) {
        this.ropeLength = ropeLength;
    }

    public double getRopeThickness() {
        return ropeThickness;
    }

    public void setRopeThickness(double ropeThickness) {
        this.ropeThickness = ropeThickness;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public double getDamping() {
        return damping;
    }

    public void setDamping(double damping) {
        this.damping = damping;
    }

    public boolean isBeadsVisible() {
        return beadsVisible;
    }

    public void setBeadsVisible(boolean beadsVisible) {
        this.beadsVisible = beadsVisible;
    }

    public boolean isShadowsVisible() {
        return shadowsVisible;
    }

    public void setShadowsVisible(boolean shadowsVisible) {
        this.shadowsVisible = shadowsVisible;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public boolean isLaunchAtStartup() {
        return launchAtStartup;
    }

    public void setLaunchAtStartup(boolean launchAtStartup) {
        this.launchAtStartup = launchAtStartup;
    }

    public int getSelectedMonitorIndex() {
        return selectedMonitorIndex;
    }

    public void setSelectedMonitorIndex(int selectedMonitorIndex) {
        this.selectedMonitorIndex = selectedMonitorIndex;
    }

    public double getOverlayOffsetX() {
        return overlayOffsetX;
    }

    public void setOverlayOffsetX(double overlayOffsetX) {
        this.overlayOffsetX = overlayOffsetX;
    }

    public double getOverlayOffsetY() {
        return overlayOffsetY;
    }

    public void setOverlayOffsetY(double overlayOffsetY) {
        this.overlayOffsetY = overlayOffsetY;
    }
}
