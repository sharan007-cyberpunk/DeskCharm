package com.sharan.deskcharm.charm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CharmLibrary {
    private final List<Charm> charms = new ArrayList<>();

    public CharmLibrary() {
        charms.add(new Charm("star", "Star", CharmType.STAR, 30));
        charms.add(new Charm("moon", "Moon", CharmType.MOON, 30));
        charms.add(new Charm("planet", "Planet", CharmType.PLANET, 31));
        charms.add(new Charm("diamond", "Diamond", CharmType.DIAMOND, 30));
        charms.add(new Charm("leaf", "Leaf", CharmType.LEAF, 30));
    }

    public List<Charm> all() {
        return Collections.unmodifiableList(charms);
    }

    public Charm find(String id) {
        return charms.stream().filter(c -> c.id().equals(id)).findFirst().orElse(charms.get(0));
    }

    public void add(Charm charm) {
        charms.removeIf(c -> c.id().equals(charm.id()));
        charms.add(charm);
    }
}
