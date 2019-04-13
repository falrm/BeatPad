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
  val heptatonicMax = notes.maxBy { it.heptatonicValue }!!.heptatonicValue
  val heptatonicMin = notes.minBy { it.heptatonicValue }!!.heptatonicValue
  fun covers(note: Note): Boolean = note.heptatonicValue in heptatonicMin..heptatonicMax
  fun ledgersTo(note: Note) = if(note.heptatonicValue > heptatonicMax) {
    ledgers.filter { it.heptatonicValue > heptatonicMax && it.heptatonicValue <= note.heptatonicValue }
  } else {
    ledgers.filter { it.heptatonicValue < heptatonicMin && it.heptatonicValue >= note.heptatonicValue }
  }
//  val min = min(notes)
//  val max = max(notes)
  companion object {
    val ledgers: List<Note> = listOf(
      note(C,4),

      note(A,5),
      note(C,6),
      note(E,6),
      note(G,6),
      note(B,6),
      note(D,7),
      note(F,7),
      note(A,7),
      note(C,8),
      note(E,8),
      note(G,8),
      note(B,8),

      note(E,2),
      note(C,2),
      note(A,1),
      note(F,1),
      note(D,1),
      note(B,0),
      note(G,0),
      note(E,0),
      note(C,0)
    )
  }
}