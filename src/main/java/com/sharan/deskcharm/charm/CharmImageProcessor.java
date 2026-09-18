package com.sharan.deskcharm.charm;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validates and prepares a user-selected PNG/JPG/JPEG file for use as a
 * custom charm: checks the extension and that it actually decodes as an
 * image, resizes it to fit the charm's target on-screen size while
 * preserving aspect ratio, and copies the result into the app's storage
 * directory so it survives independently of the original file's location.
 *
 * Background removal is NOT implemented in this stage — importing a JPG or a
 * PNG without transparency will show its full rectangular (or naturally
 * transparent, for PNG) bounds as the charm. This class does not claim any
 * AI-based background removal capability.
 */
public class CharmImageProcessor {

    private static final Pattern ALLOWED_EXTENSIONS = Pattern.compile("(?i).*\\.(png|jpg|jpeg)$");
    private static final int MAX_DIMENSION_PX = 256;

    public static class ImportException extends Exception {
        public ImportException(String message) {
            super(message);
        }

        public ImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Validates, resizes, and copies {@code sourceFile} into {@code storageDirectory}.
     *
     * @return the path of the newly stored, processed image file.
     */
    public Path importImage(File sourceFile, Path storageDirectory) throws ImportException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new ImportException("The selected file does not exist or is not a regular file.");
        }
        if (!ALLOWED_EXTENSIONS.matcher(sourceFile.getName()).matches()) {
            throw new ImportException("Unsupported file type. Please choose a PNG, JPG, or JPEG image.");
        }

        Image sourceImage;
        try {
            sourceImage = new Image(sourceFile.toURI().toString());
        } catch (Exception e) {
            throw new ImportException("The file could not be read as an image: " + sourceFile.getName(), e);
        }
        if (sourceImage.isError() || sourceImage.getWidth() <= 0 || sourceImage.getHeight() <= 0) {
            throw new ImportException("The image appears to be invalid or corrupted: " + sourceFile.getName());
        }

        WritableImage resized = resizePreservingAspectRatio(sourceImage, MAX_DIMENSION_PX);

        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new ImportException("Could not create the charm storage directory.", e);
        }

        String targetFileName = UUID.randomUUID() + ".png";
        Path targetPath = storageDirectory.resolve(targetFileName);
        try {
            ImageIO.write(
                    javafx.embed.swing.SwingFXUtils.fromFXImage(resized, null),
                    "png",
                    targetPath.toFile()
            );
        } catch (IOException e) {
            throw new ImportException("Failed to save the processed image to the app data directory.", e);
        }

        return targetPath;
    }

    /**
     * Resizes {@code source} so its longest side is at most {@code maxDimension}
     * pixels, preserving aspect ratio, and preserving alpha (transparent PNGs
     * stay transparent).
     */
    private WritableImage resizePreservingAspectRatio(Image source, int maxDimension) {
        double width = source.getWidth();
        double height = source.getHeight();
        double scale = Math.min(1.0, maxDimension / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        PixelReader reader = source.getPixelReader();
        WritableImage output = new WritableImage(targetWidth, targetHeight);
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < targetHeight; y++) {
            int sourceY = Math.min((int) (y / scale), (int) height - 1);
            for (int x = 0; x < targetWidth; x++) {
                int sourceX = Math.min((int) (x / scale), (int) width - 1);
                writer.setArgb(x, y, reader.getArgb(sourceX, sourceY));
            }
        }
        return output;
    }
}
