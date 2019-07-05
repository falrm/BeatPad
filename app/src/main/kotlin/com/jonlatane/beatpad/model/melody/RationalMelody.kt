package com.jonlatane.beatpad.model.melody

import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.util.between01
import com.jonlatane.beatpad.util.mod12
import java.util.*

data class RationalMelody(
  override val changes: NavigableMap<Int, RationalMelody.Element> = TreeMap(),
  /** A value of 4 would indicate sixteenth notes in 4/4 time */
  override var subdivisionsPerBeat: Int = 1,
  override var limitedToNotesInHarmony : Boolean = true,
  override var drumPart : Boolean = !limitedToNotesInHarmony,
  override var shouldConformWithHarmony: Boolean = false,
  override var tonic: Int = 0,
  override var length: Int = 1,
  override var id: UUID = UUID.randomUUID(),
  override var relatedMelodies: MutableSet<UUID> = mutableSetOf()
) : Melody<RationalMelody.Element> {
  data class Element(
    var tones: MutableSet<Int> = mutableSetOf(),
    var velocity: Float = 1f
  ) : Melody.Element<RationalMelody, RationalMelody.Element> {
    override fun transpose(interval: Int): RationalMelody.Element {
      return RationalMelody.Element(
        tones = tones.map { it + interval }.toMutableSet(),
        velocity = velocity
      )
    }

    override fun offsetUnder(chord: Chord, melody: RationalMelody) = when {
      melody.shouldConformWithHarmony -> {
        chord.root.mod12.let { root ->
          when {
            root > 6 -> root - 12
            else -> root
          }
        }
      }
      else -> 0
    }
  }

  override var velocityFactor: Float = 1f
    set(value) {
      field = value.between01
    }
  override val type get() = "rational"

  override fun transpose(interval: Int): RationalMelody {
    return RationalMelody(
      shouldConformWithHarmony = shouldConformWithHarmony,
      subdivisionsPerBeat = subdivisionsPerBeat,
      tonic = tonic,
      length = length,
      changes = TreeMap(changes.mapValues { it.value.transpose(interval) })
    )
  }
}