package com.jonlatane.beatpad.output.instrument.audiotrack.generator.fft

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackGenerator

/**
 * Created by jonlatane on 7/19/15.
 */
class PitchShifterGenerator(private val audioData: FloatArray) : AudioTrackGenerator {

    override fun getAudioTrackFor(note: Int, onFail: () -> Unit): AudioTrack {
        return AudioTrack(
                AudioManager.STREAM_MUSIC,
                AudioTrackGenerator.Companion.NATIVE_OUTPUT_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT,
                0,
                AudioTrack.MODE_STATIC,
                0)
    }
}
