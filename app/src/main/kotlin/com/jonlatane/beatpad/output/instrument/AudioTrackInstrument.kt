package com.jonlatane.beatpad.output.instrument

import android.media.AudioTrack
import com.jonlatane.beatpad.model.Instrument

import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackGenerator

/**
 * Created by jonlatane on 5/5/17.
 */

class AudioTrackInstrument(private val generator: AudioTrackGenerator) : Instrument {
    private val tracks = mutableMapOf<Int, AudioTrack>()
    override fun play(tone: Int) {
        val track = AudioTrackCache.getAudioTrackForNote(tone, generator)
        track.play()
        AudioTrackCache.normalizeVolumes()
        tracks.put(tone, track)
    }

    override fun stop() {
        for (track in tracks.values) {
            track.pause()
            track.playbackHeadPosition = 0
            AudioTrackCache.normalizeVolumes()
        }
        tracks.clear()
    }
    override fun play(tone: Int, velocity: Int) {
        val track = AudioTrackCache.getAudioTrackForNote(tone, generator)
        track.play()
        AudioTrackCache.normalizeVolumes()
        tracks.put(tone, track)
    }

    override fun stop(tone: Int) {
        val track = tracks[tone]
        track?.pause()
        track?.playbackHeadPosition = 0
        AudioTrackCache.normalizeVolumes()
        tracks.remove(tone)
    }
}
