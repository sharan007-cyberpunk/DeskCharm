package com.sharan.deskcharm.charm;

public record Charm(
        String id,
        String name,
        CharmType type,
        double radius,
        String imagePath) {

    public Charm(String id, String name, CharmType type, double radius) {
        this(id, name, type, radius, null);
    }
}
