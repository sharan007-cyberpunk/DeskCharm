package com.sharan.deskcharm.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SettingsStore {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path file = Path.of(System.getenv().getOrDefault("APPDATA", System.getProperty("user.home")),
            "DeskCharm", "settings.json");

    public AppSettings load() {
        try {
            if (!Files.exists(file)) return AppSettings.defaults();
            AppSettings value = gson.fromJson(Files.readString(file), AppSettings.class);
            return value == null ? AppSettings.defaults() : sanitize(value);
        } catch (Exception e) {
            return AppSettings.defaults();
        }
    }

    public void save(AppSettings settings) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, gson.toJson(sanitize(settings)));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save settings", e);
        }
    }

    private AppSettings sanitize(AppSettings s) {
        AppSettings d = AppSettings.defaults();
        return new AppSettings(
                s.selectedCharm() == null ? d.selectedCharm() : s.selectedCharm(),
                Math.max(1, Math.min(100, s.segments())),
                Math.max(5, Math.min(50, s.segmentLength())),
                Math.max(0, Math.min(3000, s.gravity())),
                Math.max(0.8, Math.min(1.0, s.damping())),
                Math.max(1, Math.min(30, s.constraintIterations())),
                Math.max(1.0, Math.min(1.1, s.maxStretch())),
                s.beads(), s.shadows(), s.sound(), s.animation(),
                Math.max(0, s.screenIndex()),
                Math.max(0.5, Math.min(2.0, s.charmScale() <= 0 ? d.charmScale() : s.charmScale())),
                s.dangleText() == null ? d.dangleText() : s.dangleText());
    }
}
