package com.jonlatane.beatpad.output.instrument

import be.tarsos.dsp.io.android.AudioDispatcherFactory

/**
 * Created by jonlatane on 5/29/17.
 */
class TarsosInstrument(val adp: AudioDispatcherFactory): Instrument {
  override fun play(tone: Int) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun stop() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  companion object {
    //val rbs = RubberBandAu
  }
}