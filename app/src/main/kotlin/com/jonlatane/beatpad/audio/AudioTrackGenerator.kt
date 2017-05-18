package com.jonlatane.beatpad.audio

import android.media.AudioManager
import android.media.AudioTrack

/**
 * Created by jonlatane on 7/19/15.
 */
interface AudioTrackGenerator {

    fun getAudioTrackFor(note: Int): AudioTrack

    companion object {
        val NATIVE_OUTPUT_SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC) //per second
    }
}
