package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.*

// Based on "Animal Spirits" by Vulfpeck
object AnimalSpirits : Orbit {
  override fun forward(c: Chord) = when {
  // Db -> Cm7b5
    c.isMajor && !c.isDominant && !c.hasMinor2 -> Chord(c.root - 1, min7flat5)
  // Cm7b5 -> F(b9)
    c.isMinor && c.hasDiminished5 -> Chord(c.root + 5, MajAddFlat9)
  // F(b9) -> Bbm7
    c.isMajor && !c.isDominant && c.hasMinor2 -> Chord(c.root + 5, min7)
  // Bbm7 -> Eb7
    c.isMinor && !c.hasDiminished5 && !c.hasMajor2 -> Chord(c.root + 5, Dom7)
  // Eb7 -> Ebm9
    c.isDominant -> Chord(c.root, min9)
  // Ebm9 -> Ab9sus
    c.isMinor && c.hasMinor7 && c.hasMajor2 -> Chord(c.root + 5, Dom9sus)
  // Ab9sus -> Db
    c.isSus -> Chord(c.root + 5, Maj)

    else -> c
  }

  override fun back(c: Chord) = when {
  // Db -> Ab9sus
    c.isMajor && !c.isDominant && !c.hasMinor2 -> Chord(c.root - 5, Dom9sus)
  // Cm7b5 -> Db
    c.isMinor && c.hasDiminished5 -> Chord(c.root + 1, Maj)
  // F(b9) -> Cm7b5
    c.isMajor && !c.isDominant && c.hasMinor2 -> Chord(c.root - 5, min7flat5)
  // Bbm7 -> F(b9)
    c.isMinor && !c.hasDiminished5 && !c.hasMajor2 -> Chord(c.root - 5, MajAddFlat9)
  // Eb7 -> Bbm7
    c.isDominant -> Chord(c.root - 5, min7)
  // Ebm9 -> Eb7
    c.isMinor && c.hasMinor7 && c.hasMajor2 -> Chord(c.root, Dom7)
  // Ab9sus -> Ebm9
    c.isSus -> Chord(c.root - 5, min9)

    else -> c
  }
}