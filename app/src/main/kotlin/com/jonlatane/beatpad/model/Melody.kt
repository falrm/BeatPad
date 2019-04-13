package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.mod12
import java.util.*

interface Melody<ElementType : Transposable<ElementType>> : Pattern<ElementType> {
  var id: UUID
  /**
   * For drum parts, mainly. Also solos you really don't want modified. Indicates
   * the playback mechanism shouldn't "round" to the nearest in-chord note.
   */
  var limitedToNotesInHarmony: Boolean
  /**
   * For bass parts, mainly. Indicates this melody should follow the chord
   * progression. So if this melody is playing a C when you're on a C major,
   * it should play a D when you're on a D minor.
   */
  var shouldConformWithHarmony: Boolean
  var velocityFactor: Float
  val type get() = "base"

  fun offsetUnder(chord: Chord) = when {
    shouldConformWithHarmony -> {
      chord.root.mod12.let { root ->
        when {
          root > 6 -> root - 12
          else     -> root
        }
      }
    }
    else                     -> 0
  }

  fun playbackToneUnder(tone: Int, chord: Chord): Int = (tone + offsetUnder(chord))
    .let { transposedTone ->
      when {
        limitedToNotesInHarmony -> chord.closestTone(transposedTone)
        else                    -> transposedTone
      }
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
    > : Element<MelodyType, ElementType> {

    override fun offsetUnder(chord: Chord, melody: MelodyType) = when {
      melody.shouldConformWithHarmony -> {
        chord.root.mod12.let { root ->
          when {
            root > 6 -> root - 12
            else     -> root
          }
        }
      }
      else                            -> 0
    }
  }
}