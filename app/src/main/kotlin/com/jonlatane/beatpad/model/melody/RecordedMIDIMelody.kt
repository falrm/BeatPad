package com.jonlatane.beatpad.model.melody

import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.util.between01
import java.util.*

data class RecordedMIDIMelody(
  override val changes: NavigableMap<Int, RecordedMIDIMelody.Element> = TreeMap(),
  /** A value of 4 would indicate sixteenth notes in 4/4 time */
  override var subdivisionsPerBeat: Int = 1,
  override var shouldConformWithHarmony: Boolean = false,
  override var tonic: Int = 0,
  override var length: Int = 1,
  override var id: UUID = UUID.randomUUID()
) : Melody<RecordedMIDIMelody.Element> {
  data class Element(
    var attacks: MutableMap<Int, Float> = mutableMapOf(),
    var releases: MutableMap<Int, Float> = mutableMapOf()
  ) : Melody.BaseElement<RecordedMIDIMelody, RecordedMIDIMelody.Element>() {
    override fun transpose(interval: Int): RecordedMIDIMelody.Element {
      return RecordedMIDIMelody.Element(
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

  override fun transpose(interval: Int): RecordedMIDIMelody {
    return RecordedMIDIMelody(
      shouldConformWithHarmony = shouldConformWithHarmony,
      subdivisionsPerBeat = subdivisionsPerBeat,
      tonic = tonic,
      length = length,
      changes = TreeMap(changes.mapValues { it.value.transpose(interval) })
    )
  }
}