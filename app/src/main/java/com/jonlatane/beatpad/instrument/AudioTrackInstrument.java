package com.jonlatane.beatpad.instrument;

import android.media.AudioTrack;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.audio.AudioTrackGenerator;

import java.util.LinkedList;
import java.util.List;

import static com.jonlatane.beatpad.audio.AudioTrackCache.getAudioTrackForNote;

/**
 * Created by jonlatane on 5/5/17.
 */

public final class AudioTrackInstrument implements Instrument {
    private final AudioTrackGenerator generator;
    private final List<AudioTrack> tracks = new LinkedList<>();
    public AudioTrackInstrument(AudioTrackGenerator generator) {
        this.generator = generator;
    }
    @Override
    public void play(int tone) {
        AudioTrack track = getAudioTrackForNote(tone, generator);
        track.play();
        AudioTrackCache.normalizeVolumes();
        tracks.add(track);
    }
    @Override
    public void stop() {
        for(AudioTrack track : tracks) {
            track.pause();
            track.setPlaybackHeadPosition(0);
            AudioTrackCache.normalizeVolumes();
        }
        tracks.clear();
    }
}
