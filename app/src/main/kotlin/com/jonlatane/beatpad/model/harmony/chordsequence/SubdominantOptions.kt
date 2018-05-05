package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj6
import com.jonlatane.beatpad.model.harmony.chord.min7

object SubdominantOptions : Orbit {
  override fun forward(c: Chord) = when {
  //c.isDominant -> Chord(c.root + 7, min7) // V-ii
    c.isMajor -> Chord(c.root + 5, Maj6) // I-IV
    else -> c
  }

  override fun back(c: Chord) = when {
  //c.isDominant -> Chord(c.root - 2, Maj7) // V-IV
    c.isMajor -> Chord(c.root - 1, min7) // IV-iii
    else -> c
  }
}
