package com.jonlatane.beatpad.model

class RecordedToneSequence(
  override val subdivisions: MutableList<ToneSequence.Subdivision>,
  override var subdivisionsPerBeat: Int = 1,
  override var relativeTo: Int =0
) : ToneSequence