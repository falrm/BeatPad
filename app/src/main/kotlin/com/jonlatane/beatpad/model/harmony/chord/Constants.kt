package com.jonlatane.beatpad.model.harmony.chord

// Constants for actual intervals, in half elements.  dim/minor/Perfect/Major/Aug 2-7
const val root = 0
@get:JvmName("minor2")
const val m2 = 1
const val M2 = 2
const val A2 = 3
@get:JvmName("minor3")
const val m3 = 3
const val M3 = 4
const val d4 = 4
const val P4 = 5
const val A4 = 6
const val d5 = 6
const val P5 = 7
const val A5 = 8
@get:JvmName("minor6")
const val m6 = 8
const val M6 = 9
const val A6 = 10
const val d7 = 9
@get:JvmName("minor7")
const val m7 = 10
const val M7 = 11

// Traditional chord extensions
val Maj = intArrayOf(root, M3, P5)
val Maj6 = Maj + M6
val Maj69 = Maj6 + M2
val MajAdd9 = Maj + M2
val MajAddFlat9 = Maj + m2
val Maj7 = Maj + M7
val Maj9 = Maj7 + M2
val Maj11 = Maj9 + P4
val Maj13 = Maj9 + M6 // The "practical" definition, avoiding P4
val Maj13add11 = Maj11 + M6
val Maj13Sharp11 = Maj9 + M6 + A4
val min = intArrayOf(root, m3, P5)
val min7 = intArrayOf(root, m3, P5, m7)
val min7flat5 = intArrayOf(root, m3, d5, m7)
val min7Add11 = min7 + P4
val min9 = min7 + M2
val min11 = min9 + P4
val min11Flat9 = min7 + m2 + P4
val min13 = min11 + M6
val min11Flat13 = min11 + m6
val minMaj7 = intArrayOf(root, m3, P5, M7)
val Dom7 = intArrayOf(root, M3, P5, m7)
val Dom7Flat5 = intArrayOf(root, M3, d5, m7)
val Dom7Sharp5 = intArrayOf(root, M3, A5, m7)
val Dom9 = Dom7 + M2
val Dom9Flat5 = Dom7Flat5 + M2
val Dom9Sharp5 = Dom7Sharp5 + M2
val Dom11 = Dom9 + P4
val Dom13 = Dom11 + M6
val dim = intArrayOf(root, m3, d5)
val halfDim7 = min7flat5
val dim7 = dim + d7
val Aug = intArrayOf(root, M3, A5)

// Sus chords

private fun susOf(extension: IntArray): IntArray = extension.map {
  when (it) {
    m3, M3 -> P4
    else -> it
  }
}.toIntArray()
val sus = susOf(Maj)
val sus7 = susOf(Dom7)
val sus9 = susOf(Dom9)
val sus11 = susOf(Dom11)
val sus13 = susOf(Dom13)
val Maj6sus = susOf(Maj6)
val Dom7sus = susOf(Dom7)
val Dom9sus = susOf(Dom9)

// Modern harmonic modes
val Ionian = Maj13add11
val Dorian = min13
val Phrygian = min11Flat9 + m6
val Lydian = Maj13Sharp11
val Mixolydian = Dom13
val Aeolian = min11Flat13
val Locrian = dim + m2 + P4 + M6 + m7


// Constants used to differentiate between interval properties
val DIMINISHED = 1
val MINOR = 2
val PERFECT = 3
val MAJOR = 4
val AUGMENTED = 5
val NONEXISTENT = 0


