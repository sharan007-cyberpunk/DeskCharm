package com.sharan.deskcharm.windows;

import com.sharan.deskcharm.app.AppEnvironment;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owns the lifecycle of the {@link OverlayWindow} and centralizes
 * multi-monitor bookkeeping so other windows/controllers don't have to
 * duplicate screen-selection logic.
 *
 * NOTE ON CLICK-THROUGH: plain JavaFX has no public API to make part of a
 * Stage "click-through" and another part interactive. On Windows this is
 * normally solved by extending the native window style with
 * {@code WS_EX_LAYERED | WS_EX_TRANSPARENT} via the Win32 API, toggled at
 * runtime through JNA (no native compilation required, unlike raw JNI)
 * depending on whether the cursor is currently over the charm's hit-box.
 * This build ships with the approximation that the overlay window is kept
 * intentionally small (just large enough to contain the rope's swing
 * radius) rather than covering the full screen, which keeps unintended
 * desktop-blocking to a minimum without a native dependency. The
 * JNA-based {@code WS_EX_TRANSPARENT} toggle is the recommended follow-up —
 * see README.md, "Windows limitations" — together with the exact Win32
 * calls needed ({@code SetWindowLong}/{@code GetWindowLong} with
 * {@code GWL_EXSTYLE}).
 */
public final class WindowManager {

    private static final Logger LOGGER = Logger.getLogger(WindowManager.class.getName());

    private final AppEnvironment environment;
    private OverlayWindow overlayWindow;

    public WindowManager(AppEnvironment environment) {
        this.environment = environment;
    }

    public void launchOverlay() {
        try {
            overlayWindow = new OverlayWindow(new Stage(), environment);
            overlayWindow.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to launch overlay window", e);
            throw new IllegalStateException("Could not start the DeskCharm overlay", e);
        }
    }

    /**
     * Applies settings changes that require rebuilding the physics simulation
     * (rope length, gravity, damping) or the charm selection by tearing down
     * and relaunching the overlay window. Simpler visibility toggles (beads,
     * shadows, animation) are picked up live by {@link OverlayWindow#refreshFromSettings()}
     * without a full relaunch.
     */
    public void applySettingsChanges(boolean requiresRebuild) {
        if (requiresRebuild) {
            environment.rebuildSimulation();
            environment.persistSettings();
            if (overlayWindow != null) {
                overlayWindow.stopAnimation();
                overlayWindow.hide();
            }
            launchOverlay();
        } else {
            environment.persistSettings();
            if (overlayWindow != null) {
                overlayWindow.refreshFromSettings();
            }
        }
    }

    public void toggleOverlayVisibility() {
        if (overlayWindow == null) {
            launchOverlay();
            return;
        }
        if (overlayWindow.isShowing()) {
            overlayWindow.hide();
        } else {
            overlayWindow.show();
        }
    }

    public void shutdown() {
        if (overlayWindow != null) {
            overlayWindow.stopAnimation();
            overlayWindow.hide();
        }
    }

    public OverlayWindow getOverlayWindow() {
        return overlayWindow;
    }

    public List<Screen> getAvailableScreens() {
        return Screen.getScreens();
    }

    public void resetOverlayPosition() {
        environment.getSettings().setOverlayOffsetX(0.0);
        environment.getSettings().setOverlayOffsetY(0.0);
        environment.persistSettings();
        if (overlayWindow != null) {
            overlayWindow.hide();
            launchOverlay();
        }
    }
}
