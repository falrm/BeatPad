package com.jonlatane.beatpad.model

import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.model.melody.RecordedMIDIMelody
import com.jonlatane.beatpad.util.mod12
import java.util.*

/**
 * Base type for melodies, a thing that is to be played back once or more atop the [Harmony] in
 * a [Section]. Types include:
 *
 * * [RationalMelody]: MIDI melody limited by its [ElementType] to what can be rendered as a single
 *   "voice" in classical counterpoint (or your Finale, Sibelius, MuseScores of the world). Can be
 *   recorded at 1-24 subdivisions per beat, with clever mappings for weird shit composers do like
 *   triplets, quintuplets, 17lets or whatever. Just not over 24, for now, like MIDI beat clock.
 * * [RecordedMIDIMelody]: Arbitrary NOTE_ON and NOTE_OFF events recorded
 * * Audio file/recording support should be a thing, including cloud service integration if it can
 *   be done legally and stuff
 *
 * For serialization purposes, [Melody.type] should be overridden at the class level.
 */
interface Melody<ElementType : Transposable<ElementType>> : Pattern<ElementType> {
  var id: UUID
  var relatedMelodies: MutableSet<UUID>
  /**
   * This was initially used to represent drum parts. But 
   * For drum parts, mainly. Also solos you really don't want modified. Indicates
   * the playback mechanism shouldn't "round" to the nearest in-chord note.
   */
  var limitedToNotesInHarmony: Boolean
  var drumPart: Boolean
  /**
   * For bass parts, mainly. Indicates this melody should follow the chord
   * progression. So if this melody is arpeggiating C, E, G on CM7 and [shouldConformWithHarmony]
   * is true, it would arpeggiate D, F, A on Dm7. If false, it would arpeggiate the nearest notes
   * to C, E, G without transposing, so C, F, F (given our "rounding down" rule).
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