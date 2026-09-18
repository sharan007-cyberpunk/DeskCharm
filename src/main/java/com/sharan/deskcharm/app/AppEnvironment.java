package com.sharan.deskcharm.app;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmLibrary;
import com.sharan.deskcharm.charm.CustomCharmStore;
import com.sharan.deskcharm.physics.RopeConfiguration;
import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.physics.Vector2;
import com.sharan.deskcharm.settings.AppSettings;
import com.sharan.deskcharm.settings.SettingsStore;

/**
 * Explicit, instance-based dependency container for the running application.
 * Exactly one {@code AppEnvironment} is created in
 * {@code DeskCharmApplication.start(...)} and passed to whatever windows and
 * controllers need it — deliberately avoiding the global mutable singletons
 * the project spec calls out to avoid.
 */
public final class AppEnvironment {

    private final SettingsStore settingsStore;
    private final AppSettings settings;
    private final CharmLibrary charmLibrary;
    private final CustomCharmStore customCharmStore;
    private RopeSimulation ropeSimulation;

    public AppEnvironment() {
        this.settingsStore = new SettingsStore();
        this.settings = settingsStore.load();
        this.charmLibrary = new CharmLibrary();
        this.customCharmStore = new CustomCharmStore(settingsStore.getAppDataDirectory().resolve("charms"));

        for (Charm previouslyImported : customCharmStore.loadAll()) {
            charmLibrary.register(previouslyImported);
        }
        if (charmLibrary.findById(settings.getSelectedCharmId()).isPresent()) {
            charmLibrary.selectCharm(settings.getSelectedCharmId());
        }
        this.ropeSimulation = createSimulationFromSettings(
                new Vector2(settings.getOverlayOffsetX(), settings.getOverlayOffsetY()));
    }

    public SettingsStore getSettingsStore() {
        return settingsStore;
    }

    public AppSettings getSettings() {
        return settings;
    }

    public CharmLibrary getCharmLibrary() {
        return charmLibrary;
    }

    public RopeSimulation getRopeSimulation() {
        return ropeSimulation;
    }

    public Charm getSelectedCharm() {
        return charmLibrary.getSelectedCharm();
    }

    public void selectCharm(Charm charm) {
        charmLibrary.selectCharm(charm.getId());
        settings.setSelectedCharmId(charm.getId());
        settingsStore.save(settings);
    }

    /**
     * Registers a newly imported custom-image charm with the library and
     * persists the updated custom-charm index so it survives a restart.
     */
    public void registerCustomCharm(Charm charm) {
        charmLibrary.register(charm);
        java.util.List<Charm> customCharms = charmLibrary.getAllCharms().stream()
                .filter(c -> !charmLibrary.isBuiltIn(c.getId()))
                .toList();
        customCharmStore.saveAll(customCharms);
    }

    /**
     * Rebuilds the physics simulation from the current settings — e.g. after
     * the user changes rope length/gravity/damping in the settings window —
     * while preserving the existing anchor position.
     */
    public void rebuildSimulation() {
        Vector2 anchor = ropeSimulation.getAnchor();
        this.ropeSimulation = createSimulationFromSettings(anchor);
    }

    public void persistSettings() {
        settingsStore.save(settings);
    }

    private RopeSimulation createSimulationFromSettings(Vector2 anchor) {
        RopeConfiguration base = RopeConfiguration.defaults();
        double segmentLength = settings.getRopeLength() / base.getSegmentCount();
        RopeConfiguration config = base
                .withSegmentLength(segmentLength)
                .withGravityY(settings.getGravity())
                .withDamping(settings.getDamping());
        return new RopeSimulation(config, anchor);
    }
}
