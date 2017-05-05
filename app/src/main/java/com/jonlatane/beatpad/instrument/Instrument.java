package com.jonlatane.beatpad.instrument;

import android.media.AudioTrack;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.audio.AudioTrackGenerator;

import static com.jonlatane.beatpad.audio.AudioTrackCache.getAudioTrackForNote;

/**
 * Created by jonlatane on 5/5/17.
 */

public final class Instrument {
    private final AudioTrackGenerator generator;
    public Instrument(AudioTrackGenerator generator) {
        this.generator = generator;
    }
    public AudioTrack play(int tone) {
        AudioTrack result = getAudioTrackForNote(tone, generator);
        result.play();
        AudioTrackCache.normalizeVolumes();
        return result;
    }
    public void stop(int tone) {
        AudioTrack track = AudioTrackCache.getAudioTrackForNote(tone, generator);
        stop(track);
        AudioTrackCache.normalizeVolumes();
    }
    public void stop(AudioTrack track) {
        if(track != null) {
            track.pause();
            track.setPlaybackHeadPosition(0);
            AudioTrackCache.normalizeVolumes();
        }
    }
}
