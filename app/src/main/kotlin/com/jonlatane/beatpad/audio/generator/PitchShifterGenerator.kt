package com.jonlatane.beatpad.audio.generator

import android.media.AudioTrack
import android.media.AudioFormat
import android.media.AudioManager

import com.jonlatane.beatpad.audio.AudioTrackGenerator

/**
 * Created by jonlatane on 7/19/15.
 */
class PitchShifterGenerator(private val audioData: FloatArray) : AudioTrackGenerator {

    override fun getAudioTrackFor(note: Int): AudioTrack {
        return AudioTrack(
                AudioManager.STREAM_MUSIC,
                AudioTrackGenerator.NATIVE_OUTPUT_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT,
                0,
                AudioTrack.MODE_STATIC,
                0)
    }
}
