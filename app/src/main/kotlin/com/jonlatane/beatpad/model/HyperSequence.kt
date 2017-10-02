package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.output.instrument.Instrument

data class HyperSequence(
  var deltas: MutableList<Delta> = mutableListOf<Delta>()
) {
  data class Delta(
    var chord: Chord = Chord(0, Maj7),
    val playback: MutableMap<Instrument, ToneSequence>
      = mutableMapOf()
  )

  companion object {
    fun Tacet(): ToneSequence {
      return RecordedToneSequence(mutableListOf())
    }
  }
}