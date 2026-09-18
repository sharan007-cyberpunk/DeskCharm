package com.sharan.deskcharm.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads and saves {@link AppSettings} as JSON under the user's Windows
 * application data directory (typically {@code %APPDATA%\DeskCharm\}).
 * There is deliberately no global mutable singleton here: whoever needs
 * settings is handed a {@code SettingsStore} instance (or the loaded
 * {@link AppSettings} it produces) explicitly, e.g. via constructor
 * injection, rather than reaching for static state.
 */
public class SettingsStore {

    private static final String SETTINGS_FILE_NAME = "settings.json";
    private static final String APP_FOLDER_NAME = "DeskCharm";

    private final Path settingsFilePath;
    private final ObjectMapper objectMapper;

    public SettingsStore() {
        this(resolveDefaultAppDataDirectory());
    }

    /** Allows tests (and alternate platforms) to point at an arbitrary directory. */
    public SettingsStore(Path appDataDirectory) {
        this.settingsFilePath = appDataDirectory.resolve(SETTINGS_FILE_NAME);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            Files.createDirectories(appDataDirectory);
        } catch (IOException e) {
            System.err.println("Could not create settings directory " + appDataDirectory
                    + "; settings will not persist between runs: " + e.getMessage());
        }
    }

    /**
     * Resolves {@code %APPDATA%\DeskCharm} on Windows, falling back to a
     * dotfile-style directory under the user's home folder on other
     * platforms (useful for running tests on non-Windows CI/dev machines).
     */
    private static Path resolveDefaultAppDataDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Paths.get(appData, APP_FOLDER_NAME);
        }
        String userHome = System.getProperty("user.home", ".");
        return Paths.get(userHome, "." + APP_FOLDER_NAME.toLowerCase());
    }

    /**
     * Loads settings from disk, repairing/clamping any invalid values found.
     * If the file does not exist or cannot be parsed as JSON, returns fresh
     * defaults instead of throwing, so a missing or corrupted settings file
     * never prevents the application from starting.
     */
    public AppSettings load() {
        if (!Files.exists(settingsFilePath)) {
            return AppSettings.defaults();
        }
        try {
            AppSettings loaded = objectMapper.readValue(settingsFilePath.toFile(), AppSettings.class);
            if (loaded == null) {
                return AppSettings.defaults();
            }
            loaded.validateAndRepair();
            return loaded;
        } catch (IOException e) {
            System.err.println("Settings file was corrupt or unreadable; falling back to defaults: "
                    + e.getMessage());
            backUpCorruptFile();
            return AppSettings.defaults();
        }
    }

    /** Renames an unreadable settings file aside so it doesn't keep failing to load, and isn't silently lost. */
    private void backUpCorruptFile() {
        try {
            Path backupPath = settingsFilePath.resolveSibling(SETTINGS_FILE_NAME + ".corrupt");
            Files.move(settingsFilePath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Best-effort only; failing to back up the corrupt file is not itself fatal.
        }
    }

    /**
     * Saves the given settings to disk as JSON. Errors are logged rather than
     * thrown, since a failed save should not crash the running application.
     */
    public void save(AppSettings settings) {
        if (settings == null) {
            return;
        }
        settings.validateAndRepair();
        try {
            objectMapper.writeValue(settingsFilePath.toFile(), settings);
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public Path getSettingsFilePath() {
        return settingsFilePath;
    }

    public Path getAppDataDirectory() {
        return settingsFilePath.getParent();
    }
}
