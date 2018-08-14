package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
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

  fun transposeInPlace(interval: Int) {
    val transposed = transpose(interval)
    transposed.changes.forEach { index, element ->
      changes[index] = element
    }
  }

  interface Element<
    MelodyType : Melody<ElementType>,
    ElementType : Transposable<ElementType>
  > : Transposable<ElementType> {
    fun offsetUnder(chord: Chord, melody: MelodyType): Int
  }
}