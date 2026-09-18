package com.sharan.deskcharm.charm;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes one selectable charm: its identity, visual shape/size/color, and
 * optional overrides (a custom imported image, a sound effect) plus any
 * per-charm physics tweaks (e.g. a heavier charm swings more slowly).
 *
 * This class is a plain, immutable data holder — no JavaFX or file-I/O
 * dependencies — so it can be unit tested and (de)serialized freely.
 */
public final class Charm {

    private final String id;
    private final String displayName;
    private final CharmType type;
    private final double size;
    private final String primaryColorHex;
    private final String secondaryColorHex;
    private final String imagePath;      // null unless type == CUSTOM_IMAGE
    private final String soundEffectPath; // null if no sound assigned
    private final double relativeMass;    // multiplier applied to the bottom rope node's mass

    public Charm(String id,
                 String displayName,
                 CharmType type,
                 double size,
                 String primaryColorHex,
                 String secondaryColorHex,
                 String imagePath,
                 String soundEffectPath,
                 double relativeMass) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.type = Objects.requireNonNull(type, "type");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (relativeMass <= 0) {
            throw new IllegalArgumentException("relativeMass must be > 0");
        }
        this.size = size;
        this.primaryColorHex = primaryColorHex;
        this.secondaryColorHex = secondaryColorHex;
        this.imagePath = imagePath;
        this.soundEffectPath = soundEffectPath;
        this.relativeMass = relativeMass;
    }

    /** Convenience factory for a built-in (non-image) charm. */
    public static Charm builtIn(String id, String displayName, CharmType type, double size,
                                 String primaryColorHex, String secondaryColorHex, double relativeMass) {
        return new Charm(id, displayName, type, size, primaryColorHex, secondaryColorHex,
                null, null, relativeMass);
    }

    /** Convenience factory for a user-imported custom-image charm. */
    public static Charm fromImage(String id, String displayName, String imagePath, double size) {
        return new Charm(id, displayName, CharmType.CUSTOM_IMAGE, size, null, null, imagePath, null, 1.0);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CharmType getType() {
        return type;
    }

    public double getSize() {
        return size;
    }

    public Optional<String> getPrimaryColorHex() {
        return Optional.ofNullable(primaryColorHex);
    }

    public Optional<String> getSecondaryColorHex() {
        return Optional.ofNullable(secondaryColorHex);
    }

    public Optional<String> getImagePath() {
        return Optional.ofNullable(imagePath);
    }

    public Optional<String> getSoundEffectPath() {
        return Optional.ofNullable(soundEffectPath);
    }

    public double getRelativeMass() {
        return relativeMass;
    }

    public Charm withSoundEffect(String newSoundEffectPath) {
        return new Charm(id, displayName, type, size, primaryColorHex, secondaryColorHex,
                imagePath, newSoundEffectPath, relativeMass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Charm other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Charm{id='" + id + "', displayName='" + displayName + "', type=" + type + '}';
    }
}
