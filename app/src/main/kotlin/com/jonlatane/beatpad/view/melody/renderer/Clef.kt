package com.jonlatane.beatpad.view.melody.renderer

import com.jonlatane.beatpad.view.melody.renderer.Note.Companion.note
import com.jonlatane.beatpad.view.melody.renderer.Note.Letter.*

enum class Clef(
  val notes: Set<Note>
) {
  TREBLE(setOf(
    note(F, 5),
    note(D, 5),
    note(B, 4),
    note(G, 4),
    note(E, 4)
  )),
  BASS(setOf(
    note(A, 3),
    note(F, 3),
    note(D, 3),
    note(B, 2),
    note(G, 2)
  ));
//  val min = min(notes)
//  val max = max(notes)
  companion object {
    val ledgers: List<Int> = listOf(
      0,
      21, 24, 28, 31, 35, 38, 41, 45,
      -20, -24, -27, -31, -34, -37, -41
    )
  }
}