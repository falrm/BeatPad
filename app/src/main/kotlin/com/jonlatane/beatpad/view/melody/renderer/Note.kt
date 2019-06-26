package com.jonlatane.beatpad.view.melody.renderer

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
    G(7, 4)
  }
  enum class Sign(
    val stringValue: String,
    val modifier: Int
  ) {
    Natural("", 0),
    Sharp("#", 1),
    DoubleSharp("##", 2),
    Flat("b", -1),
    DoubleFlat("bb", -2)
  }
  val stringValue: String get() = "${letter.name}${sign.stringValue}$octave"
  val heptatonicValue get() = 8 * octave + letter.letterOffset

  companion object {
    fun naturalOrSharpNoteFor(tone: Int): Note = notesFor[tone]!!.let {
      it.firstOrNull { it.sign == Sign.Natural}
        ?: it.first { it.sign == Sign.Sharp }
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