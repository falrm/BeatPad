package com.jonlatane.beatpad.model.orbifold.orbit

import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.Chord

object WholeSteps: Orbit {
    override fun forward(c: Chord) = Chord(c.root + 2, c.extension)
    override fun back(c: Chord) = Chord(c.root - 2, c.extension)
}