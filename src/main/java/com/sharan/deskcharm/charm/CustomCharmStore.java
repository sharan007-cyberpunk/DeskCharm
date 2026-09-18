package com.sharan.deskcharm.charm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

public final class CustomCharmStore {
    private final Path directory = Path.of(System.getenv().getOrDefault("APPDATA", System.getProperty("user.home")),
            "DeskCharm", "charms");

    public CustomCharmStore() {
        try { Files.createDirectories(directory); } catch (IOException e) {
            throw new IllegalStateException("Cannot create custom charm directory", e);
        }
    }

    public Path importImage(Path source) throws IOException {
        String safe = source.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destination = directory.resolve(System.currentTimeMillis() + "_" + safe);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }
}
