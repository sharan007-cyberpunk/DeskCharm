package com.sharan.deskcharm.app;

import com.sharan.deskcharm.charm.CharmLibrary;
import com.sharan.deskcharm.interaction.DragController;
import com.sharan.deskcharm.physics.RopeConfiguration;
import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.settings.AppSettings;
import com.sharan.deskcharm.settings.SettingsStore;
import com.sharan.deskcharm.windows.OverlayWindow;
import com.sharan.deskcharm.windows.SystemTrayManager;

public final class AppEnvironment {
    private final SettingsStore settingsStore = new SettingsStore();
    private final CharmLibrary charmLibrary = new CharmLibrary();
    private RopeSimulation simulation;
    private OverlayWindow overlay;
    private SystemTrayManager tray;

    public void start() {
        AppSettings settings = settingsStore.load();
        simulation = new RopeSimulation(new RopeConfiguration(
                settings.segments(), settings.segmentLength(), settings.gravity(),
                settings.damping(), settings.constraintIterations(), settings.maxStretch()));
        overlay = new OverlayWindow(simulation, charmLibrary, settings, settingsStore);
        overlay.show();
        tray = new SystemTrayManager(overlay, charmLibrary, settingsStore);
        tray.install();
    }

    public void stop() {
        if (tray != null) tray.uninstall();
        if (overlay != null) overlay.close();
    }
}
