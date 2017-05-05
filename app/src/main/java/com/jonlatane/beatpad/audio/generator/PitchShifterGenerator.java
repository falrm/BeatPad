package com.jonlatane.beatpad.audio.generator;

import android.media.AudioTrack;

import com.jonlatane.beatpad.audio.AudioTrackGenerator;

/**
 * Created by jonlatane on 7/19/15.
 */
public class PitchShifterGenerator implements AudioTrackGenerator {
    private final float[] audioData;

    public PitchShifterGenerator(float[] audioData) {
        this.audioData = audioData;
    }

    @Override
    public AudioTrack getAudioTrackFor(int note) {
        return null;
    }
}
