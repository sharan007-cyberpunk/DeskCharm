package com.sharan.deskcharm;

import com.sharan.deskcharm.settings.AppSettings;
import com.sharan.deskcharm.settings.SettingsStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsStoreTest {

    @Test
    void loadingWithNoExistingFileReturnsDefaults(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir.resolve("empty-dir"));
        AppSettings settings = store.load();
        assertEquals(AppSettings.DEFAULT_SELECTED_CHARM_ID, settings.getSelectedCharmId());
        assertEquals(AppSettings.DEFAULT_ROPE_LENGTH, settings.getRopeLength(), 1e-9);
    }

    @Test
    void saveThenLoadRoundTripsValues(@TempDir Path tempDir) {
        SettingsStore store = new SettingsStore(tempDir);
        AppSettings toSave = AppSettings.defaults();
        toSave.setSelectedCharmId("moon");
        toSave.setRopeLength(240.0);
        toSave.setSoundEnabled(true);
        store.save(toSave);

        SettingsStore reloadStore = new SettingsStore(tempDir);
        AppSettings loaded = reloadStore.load();
        assertEquals("moon", loaded.getSelectedCharmId());
        assertEquals(240.0, loaded.getRopeLength(), 1e-9);
        assertTrue(loaded.isSoundEnabled());
    }

    @Test
    void corruptJsonFallsBackToDefaultsInsteadOfThrowing(@TempDir Path tempDir) throws IOException {
        Files.createDirectories(tempDir);
        Path settingsFile = tempDir.resolve("settings.json");
        Files.writeString(settingsFile, "{ this is not valid json ][");

        SettingsStore store = new SettingsStore(tempDir);
        AppSettings settings = assertDoesNotThrow(store::load);
        assertEquals(AppSettings.DEFAULT_SELECTED_CHARM_ID, settings.getSelectedCharmId());
    }

    @Test
    void validateAndRepairClampsOutOfRangeValues() {
        AppSettings settings = AppSettings.defaults();
        settings.setDamping(5.0); // way out of the (0.80, 1.0] valid range
        settings.setRopeLength(-50.0);
        settings.validateAndRepair();
        assertTrue(settings.getDamping() <= 1.0);
        assertTrue(settings.getRopeLength() >= 60.0);
    }
}
