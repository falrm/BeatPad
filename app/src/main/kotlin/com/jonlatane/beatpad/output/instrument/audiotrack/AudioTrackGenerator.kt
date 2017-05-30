package com.jonlatane.beatpad.output.instrument.audiotrack

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import org.jetbrains.anko.AnkoLogger

/**
 * Created by jonlatane on 7/19/15.
 */
interface AudioTrackGenerator: AnkoLogger {

  fun getAudioTrackFor(note: Int, onFail: () -> Unit = {}): AudioTrack
  companion object {
    val NATIVE_OUTPUT_SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC) //per second
    fun createAudioSessionId(): Int {
      val track = createAudioTrack(byteArrayOf(), null)
      val result = track.audioSessionId
      track.apply { flush(); release() }
      return result
    }
    fun createAudioTrack(generatedSnd: ByteArray, audioSessionId: Int? = null):AudioTrack {
      val numFrames = generatedSnd.size / 2
      var track: AudioTrack? = null
      while (track == null) {
        try {
          track = if(audioSessionId != null) AudioTrack(AudioManager.STREAM_MUSIC,
            AudioTrackGenerator.Companion.NATIVE_OUTPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT, 2 * numFrames,
            AudioTrack.MODE_STATIC, audioSessionId)
          else AudioTrack(AudioManager.STREAM_MUSIC,
            AudioTrackGenerator.Companion.NATIVE_OUTPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT, 2 * numFrames,
            AudioTrack.MODE_STATIC)
          track.write(generatedSnd, 0, generatedSnd.size)
          track.setLoopPoints(0, numFrames, -1)
          if (track.state != AudioTrack.STATE_INITIALIZED) {
            error("Track state: ${track.state}")
          }
        } catch (e: Throwable) {
          track?.flush()
          track?.release()
          track = null
          AudioTrackCache.releaseOne()
        }

      }

      return track
    }
    fun createAudioTrack(numFrames: Int, generatedSnd: FloatArray, audioSessionId: Int? = null):AudioTrack {
      var track: AudioTrack? = null
      while (track == null) {
        try {
          track = if(audioSessionId != null) AudioTrack(AudioManager.STREAM_MUSIC,
            AudioTrackGenerator.Companion.NATIVE_OUTPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.ENCODING_PCM_FLOAT, 2 * numFrames,
            AudioTrack.MODE_STATIC, audioSessionId)
          else AudioTrack(AudioManager.STREAM_MUSIC,
            AudioTrackGenerator.Companion.NATIVE_OUTPUT_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_DEFAULT,
            AudioFormat.ENCODING_PCM_FLOAT, 2 * numFrames,
            AudioTrack.MODE_STATIC)
          track.write(generatedSnd, 0, generatedSnd.size, AudioTrack.WRITE_NON_BLOCKING)
          track.setLoopPoints(0, numFrames, -1)
          if (track.state != AudioTrack.STATE_INITIALIZED) {
            error("Track state: ${track.state}")
          }
        } catch (e: Throwable) {
          track?.flush()
          track?.release()
          track = null
          AudioTrackCache.releaseOne()
        }

      }

      return track
    }
  }
}
