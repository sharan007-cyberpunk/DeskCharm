package com.sharan.deskcharm;

import com.sharan.deskcharm.settings.AppSettings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsStoreTest {
    @Test void defaultsAreSane() {
        AppSettings s = AppSettings.defaults();
        assertEquals(20, s.segments());
        assertEquals("star", s.selectedCharm());
    }
}
