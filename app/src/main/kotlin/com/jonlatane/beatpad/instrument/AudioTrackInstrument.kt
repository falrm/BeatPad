package com.jonlatane.beatpad.instrument

import android.media.AudioTrack

import com.jonlatane.beatpad.audio.AudioTrackCache
import com.jonlatane.beatpad.audio.AudioTrackGenerator

import java.util.LinkedList

import com.jonlatane.beatpad.audio.AudioTrackCache.getAudioTrackForNote

/**
 * Created by jonlatane on 5/5/17.
 */

class AudioTrackInstrument(private val generator: AudioTrackGenerator) : Instrument {
    private val tracks = mutableListOf<AudioTrack>()
    override fun play(tone: Int) {
        val track = getAudioTrackForNote(tone, generator)
        track.play()
        AudioTrackCache.normalizeVolumes()
        tracks.add(track)
    }

    override fun stop() {
        for (track in tracks) {
            track.pause()
            track.setPlaybackHeadPosition(0)
            AudioTrackCache.normalizeVolumes()
        }
        tracks.clear()
    }
}
