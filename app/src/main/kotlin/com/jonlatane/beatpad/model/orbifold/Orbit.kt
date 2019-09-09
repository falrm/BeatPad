package com.jonlatane.beatpad.model.orbifold

/**
 * Created by jonlatane on 5/5/17.
 */
import com.jonlatane.beatpad.model.chord.*

interface Orbit {
    fun forward(c: Chord): Chord
    fun back(c: Chord): Chord

    companion object {
      fun orbit(forward: (Chord) -> Chord, back: (Chord) -> Chord) = object : Orbit {
          override fun forward(c: Chord) = forward(c)
          override fun back(c: Chord): Chord = back(c)
      }
    }
}