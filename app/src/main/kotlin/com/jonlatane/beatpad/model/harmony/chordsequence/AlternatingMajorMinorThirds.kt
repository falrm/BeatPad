package com.jonlatane.beatpad.model.harmony.chordsequence

import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj7
import com.jonlatane.beatpad.model.harmony.chord.min7

object AlternatingMajorMinorThirds : Orbit {
  override fun forward(c: Chord) = when {
    c.isDominant -> c
    c.isMinor -> Chord(c.root + 3, Maj7)
    else -> Chord(c.root + 4, min7)
  }

  override fun back(c: Chord) = when {
    c.isDominant -> c
    c.isMinor -> Chord(c.root - 4, Maj7)
    else -> Chord(c.root - 3, min7)
  }
}