package com.jonlatane.beatpad.model

/**
 * You should implement Instrument at the platform level,
 * including serialization/deserialization of your instrument
 */
interface Instrument {
    fun play(tone: Int, velocity: Int)
    fun stop(tone: Int)
    fun play(tone: Int)
    fun stop()
    val type: String get() = "To be used during deserialization"
    val instrumentName get() = "Base Instrument"
}
