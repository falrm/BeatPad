package com.jonlatane.beatpad.view.melody.renderer

import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.util.mod12

/**
 * Naming convention for notes, with C4 = 0 (i.e., Roland style, not Yamaha).
 * The note below C4 is B3 (i.e., we count our octaves by Cs, not As).
 */
data class Note private constructor(
  val letter: Letter,
  val sign: Sign = Sign.Natural,
  val octave: Int = 4
) {
  val tone: Int get() = tone(letter, sign, octave)
  enum class Letter(
    val baseTone: Int,
    val letterOffset: Int
  ) {
    A(9, 5),
    B(11, 6),
    C(0, 0),
    D(2, 1),
    E(4, 2),
    F(5, 3),
    G(7, 4);
    operator fun plus(number: Int): Letter = values().first {
      it.letterOffset == (letterOffset + number) % 7
    }
    companion object {
      fun fromNoteNameString(string: String): Letter
        = values().first { it.name.first().toLowerCase() == string.first().toLowerCase() }
    }
  }
  enum class Sign(
    val stringValue: String,
    val modifier: Int
  ) {
    None("", 0),
    Natural("", 0),
    Sharp("#", 1),
    DoubleSharp("##", 2),
    Flat("b", -1),
    DoubleFlat("bb", -2);
    companion object {
      fun fromNoteNameString(string: String): Sign = values().maxBy {
        when {
          string.endsWith(it.stringValue) -> it.stringValue.length
          else                     -> 0
        }
      }!!
    }
  }
  val stringValue: String get() = "${letter.name}${sign.stringValue}$octave"
  val heptatonicValue get() = 7 * octave + letter.letterOffset

  companion object {
    fun nameNoteUnderChord(tone: Int, chord: Chord): Note {
      val rootNote = note(
        letter = Letter.fromNoteNameString(chord.rootName),
        sign = Sign.fromNoteNameString(chord.rootName)
      )
      return when ((tone - chord.root).mod12) {
        0    -> notesFor[tone]!!.first { it.letter == rootNote.letter }
        1    -> notesFor[tone]!!.first { it.letter == rootNote.letter + 1 } //m2
        2    -> notesFor[tone]!!.first { it.letter == rootNote.letter + 1 } //M2
        3    -> notesFor[tone]!!.first {
          if (chord.hasAugmented2) {
            it.letter == rootNote.letter + 1
          } else {
            it.letter == rootNote.letter + 2
          }
        }
        4    -> notesFor[tone]!!.first { it.letter == rootNote.letter + 2 }
        5    -> notesFor[tone]!!.first { it.letter == rootNote.letter + 3 }
        6    -> notesFor[tone]!!.first {
          if (chord.hasAugmented4) {
            it.letter == rootNote.letter + 3
          } else {
            it.letter == rootNote.letter + 4
          }
        }
        7    -> notesFor[tone]!!.first { it.letter == rootNote.letter + 4 }
        8    -> notesFor[tone]!!.first {
          if (chord.hasAugmented5) {
            it.letter == rootNote.letter + 4
          } else {
            it.letter == rootNote.letter + 5
          }
        }
        9 -> notesFor[tone]!!.first { // Name dim7 chords
          if (chord.hasDiminished5 && !chord.hasMinor7 && !chord.hasMajor7) {
            it.letter == rootNote.letter + 6
          } else {
            it.letter == rootNote.letter + 5
          }
        }
        10   -> notesFor[tone]!!.first {
          if (chord.hasAugmented6) {
            it.letter == rootNote.letter + 5
          } else {
            it.letter == rootNote.letter + 6
          }
        }
        11   -> when(rootNote.sign) {
          Sign.DoubleSharp -> notesFor[tone]!!.first { it.letter == rootNote.letter }
          else -> notesFor[tone]!!.first { it.letter == (rootNote.letter + 6) }
        }
        else -> TODO()
      }
    }

    fun naturalOrSharpNoteFor(tone: Int): Note = notesFor[tone]!!.let { notesForTone ->
      notesForTone.firstOrNull { it.sign == Sign.Natural}
        ?: notesForTone.first { it.sign == Sign.Sharp }
    }

    fun naturalOrFlatNoteFor(tone: Int): Note = notesFor[tone]!!.let { notesForTone ->
      notesForTone.firstOrNull { it.sign == Sign.Natural}
        ?: notesForTone.first { it.sign == Sign.Flat }
    }

    fun note(
      letter: Letter,
      octave: Int = 4
    ): Note = note(letter, Sign.Natural, octave)

    fun note(
            letter: Letter,
            sign: Sign = Sign.Natural,
            octave: Int = 4
    ): Note = notesFor[tone(letter, sign, octave)]!!.first { it.sign == sign }

    fun tone(
      letter: Letter,
      sign: Sign,
      octave: Int
    ): Int = (octave - 4) * 12 + letter.baseTone + sign.modifier

    private val notesFor: Map<Int, List<Note>> = (-4..12).map { octave ->
      Letter.values().map { letter ->
        Sign.values().map { sign ->
          Note(letter, sign, octave)
        }
      }
    }.flatten().flatten().groupBy { it.tone }
  }
}