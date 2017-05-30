package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.DOM_7
import com.jonlatane.beatpad.harmony.chord.MAJ_7
import com.jonlatane.beatpad.harmony.chord.MIN_7
import com.jonlatane.beatpad.util.pitchClass

/**
 * Created by jonlatane on 5/26/17.
 */
enum class HarmonicFunction(var extension: IntArray) {
  TONIC(MAJ_7), SUBDOMINANT(MIN_7), DOMINANT(DOM_7), DISSONANT(intArrayOf()), TODO(intArrayOf());

  companion object {
    /**
     * Describes the transition between two chords as [TONIC], [SUBDOMINANT] or [DOMINANT].
     */
    fun between(startChord: Chord, endChord: Chord) {
      when {
        startChord.isDominant -> when {
          endChord.isDominant -> DOMINANT
          endChord.isMajor -> when ((endChord.root - startChord.root).pitchClass) {
            // Dominant is the trivial case.  We can always assume it's tonicizing the note
            // a fifth above,
            0 -> DOMINANT // Still on the V of the current key
            1 -> TONIC // to the bVI of the current key
            2 -> TONIC  // to the VI of the current key
            3 -> SUBDOMINANT // to the bVII of the current key - maybe sequencing then to b3, b5?
            4 -> TODO // to the VII of the current key - normal in minor but weird in major (viib5)
            5 -> TONIC // to I
            6 -> TODO // to bII
            7 -> TODO // to II
            8 -> TODO // to bIII
            9 -> TODO // to III
            10-> SUBDOMINANT // to IV
            11-> TODO
          }
          endChord.isMinor -> when ((endChord.root - startChord.root).pitchClass) {
            0 -> TODO // to v
            1 -> TODO // to bvi
            2 -> TODO // to vi
            3 -> TODO // to bvii
            4 -> TODO // to vii (maybe b5)
            5 -> TONIC // to i
            6 -> TODO // to ii
            7 -> TODO // to
            8 -> TODO // to
            9 -> TODO // to
            10-> TODO // to
            11-> TODO // to
          }
        }
        startChord.isMajor -> when {
          endChord.isDominant -> DOMINANT
          endChord.isMajor -> when ((endChord.root - startChord.root).pitchClass) {
            0 -> TONIC
            1 -> TODO
            2 -> TODO
            3 -> TODO
            4 -> TODO
            5 -> SUBDOMINANT
            6 -> TODO
            7 -> TONIC
            8 -> TODO
            9 -> TODO
            10-> TODO
            11-> TODO
          }
          endChord.isMinor -> when ((endChord.root - startChord.root).pitchClass) {
            0 -> TODO
            1 -> TODO
            2 -> SUBDOMINANT
            3 -> TODO
            4 -> TODO
            5 -> TODO
            6 -> TODO
            7 -> TODO
            8 -> TODO
            9 -> TODO
            10-> TODO
            11-> TODO
          }
        }
        startChord.isMinor -> when {
          endChord.isDominant -> DOMINANT
          endChord.isMajor -> when ((endChord.root - startChord.root).pitchClass) {
            0 -> TODO
            1 -> TODO
            2 -> TODO
            3 -> TODO
            4 -> TODO
            5 -> TODO
            6 -> TODO
            7 -> TODO
            8 -> TODO
            9 -> TODO
            10-> TODO
            11-> TODO
          }
          endChord.isMinor -> when ((endChord.root - startChord.root).pitchClass) {
            0 -> TODO
            1 -> TODO
            2 -> TODO
            3 -> TODO
            4 -> TODO
            5 -> TODO
            6 -> TODO
            7 -> TODO
            8 -> TODO
            9 -> TODO
            10-> TODO
            11-> TODO
          }
        }
      }
    }
  }
}