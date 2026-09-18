package com.sharan.deskcharm.charm;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;

public final class CharmImageProcessor {
    public Image load(File file, int size) {
        Image source = new Image(file.toURI().toString(), size, size, true, true);
        return removeSimpleCornerBackground(source);
    }

    private Image removeSimpleCornerBackground(Image source) {
        int w = (int) source.getWidth();
        int h = (int) source.getHeight();
        if (w <= 0 || h <= 0) return source;

        WritableImage out = new WritableImage(w, h);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = out.getPixelWriter();
        Color bg = reader.getColor(0, 0);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = reader.getColor(x, y);
                double d = Math.abs(c.getRed() - bg.getRed())
                        + Math.abs(c.getGreen() - bg.getGreen())
                        + Math.abs(c.getBlue() - bg.getBlue());
                writer.setColor(x, y, d < 0.12 ? new Color(c.getRed(), c.getGreen(), c.getBlue(), 0) : c);
            }
        }
        return out;
    }
}
