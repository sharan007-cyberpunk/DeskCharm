package com.sharan.deskcharm.charm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Curated premium charm collection. Artwork is rendered natively; no stock images are used. */
public final class CharmLibrary {
    private final List<Charm> charms = new ArrayList<>();

    public CharmLibrary() {
        charms.add(new Charm("evil-eye", "Evil Eye", CharmType.EVIL_EYE, 34));
        charms.add(new Charm("cross", "Cross", CharmType.CROSS, 34));
        charms.add(new Charm("initial-s", "S Initial", CharmType.INITIAL_S, 35));
        charms.add(new Charm("hamsa", "Hamsa", CharmType.HAMSA, 34));
        charms.add(new Charm("clover", "Clover", CharmType.CLOVER, 34));
        charms.add(new Charm("crown", "Crown", CharmType.CROWN, 35));
        charms.add(new Charm("infinity", "Infinity", CharmType.INFINITY, 35));
        charms.add(new Charm("compass", "Compass", CharmType.COMPASS, 35));
        charms.add(new Charm("lotus", "Lotus", CharmType.LOTUS, 35));
        charms.add(new Charm("feather", "Feather", CharmType.FEATHER, 35));
        charms.add(new Charm("heart", "Heart", CharmType.HEART, 34));
        charms.add(new Charm("lightning", "Lightning", CharmType.LIGHTNING, 35));
        charms.add(new Charm("sun", "Sun", CharmType.SUN, 35));
        charms.add(new Charm("maneki-neko", "Lucky Cat", CharmType.MANEKI_NEKO, 34));
        charms.add(new Charm("ganesha", "Ganesha", CharmType.GANESHA, 34));
        charms.add(new Charm("star", "Star", CharmType.STAR, 32));
        charms.add(new Charm("moon", "Moon", CharmType.MOON, 32));
        charms.add(new Charm("planet", "Planet", CharmType.PLANET, 33));
        charms.add(new Charm("diamond", "Diamond", CharmType.DIAMOND, 32));
        charms.add(new Charm("leaf", "Leaf", CharmType.LEAF, 32));
    }

    public List<Charm> all() { return Collections.unmodifiableList(charms); }
    public Charm find(String id) {
        return charms.stream().filter(c -> c.id().equals(id)).findFirst().orElse(charms.get(0));
    }
    public void add(Charm charm) {
        charms.removeIf(c -> c.id().equals(charm.id()));
        charms.add(charm);
    }
}
