package com.sharan.deskcharm;

import com.sharan.deskcharm.charm.CharmLibrary;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharmLibraryTest {
    @Test void hasBuiltInPhotoCharms() {
        CharmLibrary library = new CharmLibrary();
        assertEquals(4, library.all().size());
        assertNotNull(library.find("evil-eye"));
        assertNotNull(library.find("murugan"));
        assertEquals("KARUPPU", library.find("murugan").name());
        assertNotNull(library.find("drishti-mask"));
        assertNotNull(library.find("chili-lemon"));
    }

    @Test void addReplacesExistingIdInsteadOfDuplicating() {
        CharmLibrary library = new CharmLibrary();
        int before = library.all().size();
        library.add(new com.sharan.deskcharm.charm.Charm("evil-eye", "Evil Eye (renamed)",
                com.sharan.deskcharm.charm.CharmType.EVIL_EYE, 34, "/charms/evil_eye.png"));
        assertEquals(before, library.all().size());
        assertEquals("Evil Eye (renamed)", library.find("evil-eye").name());
    }
}
