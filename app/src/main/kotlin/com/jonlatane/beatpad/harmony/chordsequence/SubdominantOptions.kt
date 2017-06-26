package com.jonlatane.beatpad.harmony.chordsequence

import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj6
import com.jonlatane.beatpad.harmony.chord.min7

object SubdominantOptions : ChordSequence {
  override fun forward(c: Chord): Chord {
    return when {
    //c.isDominant -> Chord(c.root + 7, min7) // V-ii
      c.isMajor -> Chord(c.root + 5, Maj6) // I-IV
      else -> c
    }
  }

  override fun back(c: Chord): Chord {
    return when {
    //c.isDominant -> Chord(c.root - 2, Maj7) // V-IV
      c.isMajor -> Chord(c.root - 1, min7) // IV-iii
      else -> c
    }
  }
}
