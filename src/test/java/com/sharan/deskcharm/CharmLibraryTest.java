package com.sharan.deskcharm;

import com.sharan.deskcharm.charm.CharmLibrary;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharmLibraryTest {
    @Test void hasOriginalBuiltInCharms() {
        CharmLibrary library = new CharmLibrary();
        assertTrue(library.all().size() >= 5);
        assertNotNull(library.find("star"));
    }
}
