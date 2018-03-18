package com.jonlatane.beatpad.model

/**
 * You should implement Instrument at the platform level,
 * including serialization/deserialization of your instrument.
 *
 * Note for multi-voiced stuff like arpeggiated piano chords:
 * A rolled chord is, literally, an Instrument per note. But
 * it can work intuitively af given this.
 */
interface Instrument {
    fun play(tone: Int, velocity: Int)
    fun stop(tone: Int)
    fun play(tone: Int)
    fun stop()
    val type: String get() = "To be used during deserialization"
    val instrumentName get() = "Base Instrument"
    var volume: Float
}
