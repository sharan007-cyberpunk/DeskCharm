package com.sharan.deskcharm.charm;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persists user-imported custom charms (image path, display name, size) as a
 * small JSON index inside the app data directory, so they survive an
 * application restart. Actual image bytes are copied into the same directory
 * by {@link CharmImageProcessor} before being registered here.
 */
public class CustomCharmStore {

    private static final String INDEX_FILE_NAME = "custom-charms.json";

    private final Path storageDirectory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomCharmStore(Path storageDirectory) {
        this.storageDirectory = storageDirectory;
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create custom charm storage directory: " + storageDirectory, e);
        }
    }

    private Path indexPath() {
        return storageDirectory.resolve(INDEX_FILE_NAME);
    }

    /** Simple serializable record of one stored custom charm. */
    public static class Entry {
        public String id;
        public String displayName;
        public String imagePath;
        public double size;

        public Entry() {
            // Required for Jackson deserialization.
        }

        public Entry(String id, String displayName, String imagePath, double size) {
            this.id = id;
            this.displayName = displayName;
            this.imagePath = imagePath;
            this.size = size;
        }
    }

    /**
     * Loads previously saved custom charms. Returns an empty list (rather than
     * throwing) if the index is missing or corrupt, so a damaged file never
     * prevents the application from starting.
     */
    public List<Charm> loadAll() {
        Path path = indexPath();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            Entry[] entries = objectMapper.readValue(path.toFile(), Entry[].class);
            List<Charm> charms = new ArrayList<>();
            for (Entry entry : entries) {
                if (entry.imagePath == null || !Files.exists(Path.of(entry.imagePath))) {
                    continue; // Referenced image is missing; skip gracefully.
                }
                charms.add(Charm.fromImage(entry.id, entry.displayName, entry.imagePath, entry.size));
            }
            return charms;
        } catch (IOException e) {
            System.err.println("Custom charm index was corrupt or unreadable; ignoring it: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Persists the full given list of custom charms, overwriting the index file. */
    public void saveAll(List<Charm> customCharms) {
        List<Entry> entries = new ArrayList<>();
        for (Charm charm : customCharms) {
            entries.add(new Entry(
                    charm.getId(),
                    charm.getDisplayName(),
                    charm.getImagePath().orElse(null),
                    charm.getSize()
            ));
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexPath().toFile(), entries);
        } catch (IOException e) {
            System.err.println("Failed to save custom charm index: " + e.getMessage());
        }
    }

    public String generateNewCharmId() {
        return "custom-" + UUID.randomUUID();
    }

    public Path getStorageDirectory() {
        return storageDirectory;
    }
}
