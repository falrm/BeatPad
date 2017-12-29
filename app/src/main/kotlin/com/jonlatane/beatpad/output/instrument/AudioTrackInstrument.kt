package com.jonlatane.beatpad.output.instrument

import android.media.AudioTrack
import com.jonlatane.beatpad.model.Instrument

import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackGenerator

/**
 * Created by jonlatane on 5/5/17.
 */

class AudioTrackInstrument(private val generator: AudioTrackGenerator) : Instrument {
    private val tracks = mutableListOf<AudioTrack>()
    override fun play(tone: Int) {
        val track = AudioTrackCache.getAudioTrackForNote(tone, generator)
        track.play()
        AudioTrackCache.normalizeVolumes()
        tracks.add(track)
    }

    override fun stop() {
        for (track in tracks) {
            track.pause()
            track.playbackHeadPosition = 0
            AudioTrackCache.normalizeVolumes()
        }
        tracks.clear()
    }
}
