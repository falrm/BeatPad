package com.jonlatane.beatpad.output.instrument

import be.tarsos.dsp.io.android.AudioDispatcherFactory
import com.jonlatane.beatpad.model.Instrument

class TarsosInstrument(val adp: AudioDispatcherFactory): Instrument {
  override fun play(tone: Int) {
    TODO("not implemented")
  }

  override fun stop() {
    TODO("not implemented")
  }

  override fun play(tone: Int, velocity: Int) {
    TODO("not implemented")
  }

  override fun stop(tone: Int) {
    TODO("not implemented")
  }
}