package com.sharan.deskcharm.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plays short, simple, programmatically generated tones (not copyrighted
 * audio files) for grab/release/throw events using the Java Sound API only —
 * no external audio assets are shipped. Disabled by default via
 * {@code AppSettings.isSoundEnabled()} (defaults to {@code false}) since,
 * per the project spec, sound is optional and kept low-complexity.
 *
 * Each event plays a short sine-wave "beep" at a distinct pitch and
 * duration, generated on a background thread so audio synthesis never
 * blocks the JavaFX Application Thread.
 */
public class SoundEffectPlayer {

    private static final Logger LOGGER = Logger.getLogger(SoundEffectPlayer.class.getName());
    private static final float SAMPLE_RATE = 44100f;

    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "deskcharm-audio");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean enabled;

    public SoundEffectPlayer(boolean initiallyEnabled) {
        this.enabled = initiallyEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void playGrabbed() {
        playToneAsync(660.0, 60);
    }

    public void playReleased() {
        playToneAsync(440.0, 80);
    }

    public void playThrown() {
        playToneAsync(520.0, 50);
        playToneAsync(780.0, 90);
    }

    private void playToneAsync(double frequencyHz, int durationMillis) {
        if (!enabled) {
            return;
        }
        audioExecutor.submit(() -> playToneBlocking(frequencyHz, durationMillis));
    }

    private void playToneBlocking(double frequencyHz, int durationMillis) {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        int totalSamples = (int) (SAMPLE_RATE * durationMillis / 1000.0);
        byte[] buffer = new byte[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double angle = 2.0 * Math.PI * i * frequencyHz / SAMPLE_RATE;
            double envelope = 1.0 - (double) i / totalSamples; // simple linear fade-out to avoid clicks
            buffer[i] = (byte) (Math.sin(angle) * envelope * 90);
        }

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Sound effect playback failed; continuing without audio", e);
        }
    }

    public void shutdown() {
        audioExecutor.shutdownNow();
    }
}
