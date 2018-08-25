package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RecordedAudioMelody
import com.jonlatane.beatpad.util.mod12
import java.util.*

interface Melody<ElementType : Transposable<ElementType>> : Pattern<ElementType> {
  var id: UUID
  var shouldConformWithHarmony: Boolean
  var velocityFactor: Float
  val type get() = "base"

  fun offsetUnder(chord: Chord) = when {
    shouldConformWithHarmony -> {
      chord.root.mod12.let { root ->
        when {
          root > 6 -> root - 12
          else -> root
        }
      }
    }
    else -> 0
  }

  interface Element<
    MelodyType : Melody<ElementType>,
    ElementType : Transposable<ElementType>
  > : Transposable<ElementType> {
    fun offsetUnder(chord: Chord, melody: MelodyType): Int
  }

  abstract class BaseElement<
    MelodyType : Melody<ElementType>,
    ElementType : Transposable<ElementType>
    >: Element<MelodyType, ElementType> {

    override fun offsetUnder(chord: Chord, melody: MelodyType) = when {
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
}