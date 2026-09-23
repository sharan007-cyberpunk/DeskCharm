package com.sharan.deskcharm;

import com.sharan.deskcharm.settings.AppSettings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsStoreTest {
    @Test void defaultsAreSane() {
        AppSettings s = AppSettings.defaults();
        assertEquals(20, s.segments());
        assertEquals("evil-eye", s.selectedCharm());
        assertEquals(1.0, s.charmScale());
        assertEquals("", s.dangleText());
        assertEquals(300.0, s.ropeLength(), 0.0001);
    }

    @Test void withHelpersOnlyChangeTheTargetedField() {
        AppSettings base = AppSettings.defaults();

        AppSettings resized = base.withCharmScale(1.4);
        assertEquals(1.4, resized.charmScale());
        assertEquals(base.selectedCharm(), resized.selectedCharm());

        AppSettings recharmed = base.withSelectedCharm("murugan");
        assertEquals("murugan", recharmed.selectedCharm());
        assertEquals(base.charmScale(), recharmed.charmScale());

        AppSettings rope = base.withRopeLength(500);
        assertEquals(500.0, rope.ropeLength(), 0.0001);
        assertEquals(base.segments(), rope.segments());

        AppSettings named = base.withDangleText("Priya");
        assertEquals("Priya", named.dangleText());
        assertEquals(base.selectedCharm(), named.selectedCharm());
    }
}
