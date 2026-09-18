package com.sharan.deskcharm.charm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the set of available charms: the built-in original set (always
 * present) plus any custom charms registered at runtime (typically loaded
 * from {@code CustomCharmStore} on startup, and added to as the user imports
 * images). Order is preserved for stable display in the settings window.
 */
public class CharmLibrary {

    private final Map<String, Charm> charmsById = new LinkedHashMap<>();
    private String selectedCharmId;

    public CharmLibrary() {
        registerBuiltInCharms();
        selectedCharmId = charmsById.keySet().iterator().next();
    }

    private void registerBuiltInCharms() {
        register(Charm.builtIn("star", "Star", CharmType.STAR, 46.0, "#FFD966", "#B8860B", 1.0));
        register(Charm.builtIn("moon", "Moon", CharmType.CRESCENT, 46.0, "#E6E6FA", "#8A8AA3", 0.9));
        register(Charm.builtIn("planet", "Planet", CharmType.CIRCLE_PENDANT, 48.0, "#7FB3D5", "#2E4053", 1.2));
        register(Charm.builtIn("diamond", "Diamond", CharmType.ROUNDED_DIAMOND, 46.0, "#FFB6CE", "#A0325A", 1.0));
        register(Charm.builtIn("leaf", "Leaf", CharmType.LEAF, 44.0, "#8FCB7E", "#3E6B2C", 0.85));
    }

    public void register(Charm charm) {
        charmsById.put(charm.getId(), charm);
    }

    public boolean unregister(String charmId) {
        if (isBuiltIn(charmId)) {
            throw new IllegalArgumentException("Cannot remove a built-in charm: " + charmId);
        }
        boolean removed = charmsById.remove(charmId) != null;
        if (removed && charmId.equals(selectedCharmId)) {
            selectedCharmId = charmsById.keySet().iterator().next();
        }
        return removed;
    }

    public boolean isBuiltIn(String charmId) {
        return switch (charmId) {
            case "star", "moon", "planet", "diamond", "leaf" -> true;
            default -> false;
        };
    }

    public List<Charm> getAllCharms() {
        return Collections.unmodifiableList(new ArrayList<>(charmsById.values()));
    }

    public Optional<Charm> findById(String charmId) {
        return Optional.ofNullable(charmsById.get(charmId));
    }

    public Charm getSelectedCharm() {
        return charmsById.get(selectedCharmId);
    }

    public void selectCharm(String charmId) {
        if (!charmsById.containsKey(charmId)) {
            throw new IllegalArgumentException("Unknown charm id: " + charmId);
        }
        this.selectedCharmId = charmId;
    }

    public String getSelectedCharmId() {
        return selectedCharmId;
    }

    public int size() {
        return charmsById.size();
    }
}
