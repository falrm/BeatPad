package com.jonlatane.beatpad.output.instrument

interface Instrument {
    fun play(tone: Int)
    fun stop()
    val instrumentName get() = "Base Instrument"
}
