package com.jonlatane.beatpad.output.instrument

/**
 * Created by jonlatane on 5/8/17.
 */

interface Instrument {
    fun play(tone: Int)
    fun stop()
}
