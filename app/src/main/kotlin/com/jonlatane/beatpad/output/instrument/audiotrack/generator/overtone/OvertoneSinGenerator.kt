package com.jonlatane.beatpad.output.instrument.audiotrack.generator.overtone

import android.media.AudioTrack
import android.media.audiofx.Equalizer
import android.util.Log
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackGenerator
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackGenerator.Companion.createAudioSessionId
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackGenerator.Companion.createAudioTrack
import java.util.*

/**
 * Created by jonlatane on 7/19/15.
 */
class OvertoneSinGenerator(internal var overtones: DoubleArray = OvertoneSinGenerator.DEFAULT_OVERTONES) : AudioTrackGenerator {

  /** All [OvertoneSinGenerator]s share the same [.audioSessionId] id and [.equalizer]  */
  val audioSessionId: Int by lazy { createAudioSessionId() }
  /** All [OvertoneSinGenerator]s share the same [.audioSessionId] id and [.equalizer]  */
  val equalizer: Equalizer by lazy {
    val eq = Equalizer(1, audioSessionId)
    eq.enabled = true
    val bands = eq.numberOfBands
    Log.d("EqualizerSample", "NumberOfBands: " + bands)

    val min = eq.bandLevelRange[0]
    val max = eq.bandLevelRange[1]
    val span = (max - min).toShort()
    val midBand = (bands / 2).toShort()

    for (i in 0..bands - 1) {
      //Log.d("EqualizerSample", i + String.valueOf(equalizer!!.getCenterFreq(i) / 1000) + "Hz")
      //equalizer.setBandLevel(i, (short)((minEQLevel + maxEQLevel) / 2));

      eq.setBandLevel(i.toShort(), (min + (-.38 * Math.atan((i - midBand).toDouble()) + .5) * span).toShort())
    }
    eq
  }

  override fun getAudioTrackFor(note: Int, onFail: () -> Unit): AudioTrack {
    val pitchClass = (12 * 100 + note) % 12
    val octavesFromMiddle = (note - pitchClass) / 12
    val freq = FREQUENCIES[pitchClass] * Math.pow(2.toDouble(), octavesFromMiddle.toDouble())

    val period = 1.toDouble() / freq
    val numFrames = Math.round(period * AudioTrackGenerator.NATIVE_OUTPUT_SAMPLE_RATE).toInt()

    Log.d(TAG, "Creating track for note $note length $numFrames")

    // Generate the audio sample from a sine wave
    val sample = DoubleArray(numFrames)
    val generatedSnd = ByteArray(2 * numFrames)

    // Normalize the overtone series given so we don't overload the speaker
    var overtoneRatioSum = 0.0
    val overtonesNormalized = DoubleArray(overtones.size)
    for (d in overtones)
      overtoneRatioSum += d
    for (i in overtones.indices)
      overtonesNormalized[i] = overtones[i] / overtoneRatioSum

    // Generate our tone sample based on the normalized overtone series
    for (k in 0..numFrames - 1) {
      sample[k] = 0.0
      for (i in overtonesNormalized.indices)
        sample[k] += overtonesNormalized[i] * Math.sin((i + 1) * 2 * Math.PI * k / (AudioTrackGenerator.NATIVE_OUTPUT_SAMPLE_RATE / freq))
    }

    // convert to 16 bit pcm sound array
    // assumes the sample buffer is normalised.
    var idx = 0
    for (dVal in sample) {
      // scale to maximum amplitude
      val value = (dVal * 32767).toInt()
      // in 16 bit wav PCM, first byte is the low order byte
      generatedSnd[idx++] = (value and 0x00ff).toByte()
      generatedSnd[idx++] = (value and 0xff00).ushr(8).toByte()
    }

    // Try to make a new AudioTrack. If not possible, go through our
    // list of last used notes and
    // eliminate the LRU and try again
    return createAudioTrack(generatedSnd, audioSessionId)
  }

  override fun hashCode(): Int {
    return Arrays.hashCode(overtones)
  }

  companion object {
    /** The frequencies of the notes C4-B4  */
    private val FREQUENCIES = doubleArrayOf(261.625625, 277.1825, 293.665, 311.1275, 329.6275, 349.22875, 369.995, 391.995, 415.305, 440.0, 466.16375, 493.88375)
    private val DEFAULT_OVERTONES = doubleArrayOf(70.0, 90.0, 80.0, 10.0, 60.0, 20.0, 20.0, 1.0)

    private val TAG = "HOSGenerator"
  }
}
