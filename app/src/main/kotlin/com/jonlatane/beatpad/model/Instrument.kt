package com.jonlatane.beatpad.model

interface Instrument {
    fun play(tone: Int)
    fun stop()
    val instrumentName get() = "Base Instrument"
}
