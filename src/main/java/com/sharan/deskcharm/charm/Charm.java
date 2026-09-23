package com.sharan.deskcharm.charm;

public record Charm(
        String id,
        String name,
        CharmType type,
        double radius,
        String imagePath,
        String customText) {

    public Charm(String id, String name, CharmType type, double radius) {
        this(id, name, type, radius, null, null);
    }

    public Charm(String id, String name, CharmType type, double radius, String imagePath) {
        this(id, name, type, radius, imagePath, null);
    }

    /** Returns a copy of this charm at a different radius. Used by the size-control slider:
     *  the library keeps each charm's base size, and the active instance is scaled on selection. */
    public Charm withRadius(double newRadius) {
        return new Charm(id, name, type, newRadius, imagePath, customText);
    }
}
