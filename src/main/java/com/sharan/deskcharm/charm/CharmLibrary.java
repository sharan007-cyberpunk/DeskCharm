package com.sharan.deskcharm.charm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Curated charm collection. The four bundled charms are real photographed pieces (see
 *  src/main/resources/charms); Name Dangle charms are created at runtime from user text
 *  via add(), and user-imported CUSTOM charms arrive the same way through CustomCharmStore. */
public final class CharmLibrary {
    private final List<Charm> charms = new ArrayList<>();

    public CharmLibrary() {
        charms.add(new Charm("murugan", "KARUPPU", CharmType.MURUGAN, 42, "/charms/murugan.png"));
        charms.add(new Charm("drishti-mask", "Drishti Mask", CharmType.DRISHTI_MASK, 38, "/charms/drishti_mask.png"));
        charms.add(new Charm("evil-eye", "Evil Eye", CharmType.EVIL_EYE, 34, "/charms/evil_eye.png"));
        charms.add(new Charm("chili-lemon", "Chili & Lemon", CharmType.CHILI_LEMON, 40, "/charms/chili_lemon.png"));
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
