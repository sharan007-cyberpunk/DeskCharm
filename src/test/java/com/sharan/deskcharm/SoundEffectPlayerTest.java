package com.sharan.deskcharm;

import com.sharan.deskcharm.audio.SoundEffectPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SoundEffectPlayerTest {

    @Test
    void disabledPlayerNeverThrowsAndDoesNothing() {
        SoundEffectPlayer player = new SoundEffectPlayer(false);
        assertDoesNotThrow(player::playGrabbed);
        assertDoesNotThrow(player::playReleased);
        assertDoesNotThrow(player::playThrown);
        assertDoesNotThrow(player::shutdown);
    }

    @Test
    void enabledPlayerDoesNotThrowEvenIfNoAudioDeviceIsAvailable() {
        // In headless CI/sandbox environments there may be no audio device;
        // playback failures must be swallowed rather than propagated.
        SoundEffectPlayer player = new SoundEffectPlayer(true);
        assertDoesNotThrow(player::playGrabbed);
        assertDoesNotThrow(player::shutdown);
    }
}
