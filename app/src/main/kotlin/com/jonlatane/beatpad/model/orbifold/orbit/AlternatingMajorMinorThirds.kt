package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.chord.Maj7
import com.jonlatane.beatpad.model.chord.min7

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