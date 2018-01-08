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
    val instrumentName get() = "Base Instrument"
}
