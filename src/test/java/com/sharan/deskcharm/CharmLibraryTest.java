package com.sharan.deskcharm;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmLibrary;
import com.sharan.deskcharm.charm.CharmType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharmLibraryTest {

    @Test
    void containsAtLeastFiveBuiltInCharms() {
        CharmLibrary library = new CharmLibrary();
        assertTrue(library.getAllCharms().size() >= 5);
    }

    @Test
    void selectingAKnownCharmUpdatesSelection() {
        CharmLibrary library = new CharmLibrary();
        library.selectCharm("moon");
        assertEquals("moon", library.getSelectedCharmId());
        assertEquals(CharmType.CRESCENT, library.getSelectedCharm().getType());
    }

    @Test
    void selectingAnUnknownCharmThrows() {
        CharmLibrary library = new CharmLibrary();
        assertThrows(IllegalArgumentException.class, () -> library.selectCharm("does-not-exist"));
    }

    @Test
    void customCharmCanBeRegisteredAndSelected() {
        CharmLibrary library = new CharmLibrary();
        Charm custom = Charm.fromImage("custom-1", "My Photo", "/tmp/fake-path.png", 48.0);
        library.register(custom);
        library.selectCharm("custom-1");
        assertEquals(custom, library.getSelectedCharm());
    }

    @Test
    void builtInCharmsCannotBeUnregistered() {
        CharmLibrary library = new CharmLibrary();
        assertThrows(IllegalArgumentException.class, () -> library.unregister("star"));
    }
}
