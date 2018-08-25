package com.jonlatane.beatpad.model.melody

import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.between01
import com.jonlatane.beatpad.util.mod12
import java.util.*

data class RecordedAudioMelody(
  override val changes: NavigableMap<Int, RecordedAudioMelody.Element> = TreeMap(),
  /** A value of 4 would indicate sixteenth notes in 4/4 time */
  override var subdivisionsPerBeat: Int = 1,
  override var shouldConformWithHarmony: Boolean = false,
  override var tonic: Int = 0,
  override var length: Int = 1,
  override var id: UUID = UUID.randomUUID()
) : Melody<RecordedAudioMelody.Element> {
  data class Element(
    var attacks: MutableMap<Int, Float> = mutableMapOf(),
    var releases: MutableMap<Int, Float> = mutableMapOf()
  ) : Melody.BaseElement<RecordedAudioMelody, RecordedAudioMelody.Element>() {
    override fun transpose(interval: Int): RecordedAudioMelody.Element {
      return RecordedAudioMelody.Element(
        attacks = attacks.mapKeys { entry -> entry.key + interval }.toMutableMap(),
        releases = releases.mapKeys { entry -> entry.key + interval }.toMutableMap()
      )
    }
  }

  override var velocityFactor: Float = 1f
    set(value) {
      field = value.between01
    }
  override val type get() = "midi"

  override fun transpose(interval: Int): RecordedAudioMelody {
    return RecordedAudioMelody(
      shouldConformWithHarmony = shouldConformWithHarmony,
      subdivisionsPerBeat = subdivisionsPerBeat,
      tonic = tonic,
      length = length,
      changes = TreeMap(changes.mapValues { it.value.transpose(interval) })
    )
  }
}