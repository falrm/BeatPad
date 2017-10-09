package com.jonlatane.beatpad.harmony.chord

// Constants for actual intervals, in half subdivisions.  dim/minor/Perfect/Major/Aug 2-7
val root = 0
@get:JvmName("minor2")
val m2 = 1
val M2 = 2
val A2 = 3
@get:JvmName("minor3")
val m3 = 3
val M3 = 4
val d4 = 4
val P4 = 5
val A4 = 6
val d5 = 6
val P5 = 7
val A5 = 8
@get:JvmName("minor6")
val m6 = 8
val M6 = 9
val A6 = 10
val d7 = 9
@get:JvmName("minor7")
val m7 = 10
val M7 = 11

// Traditional chord extensions
val Maj = intArrayOf(root, M3, P5)
val Maj6 = Maj + M6
val Maj69 = Maj6 + M2
val MajAdd9 = Maj + M2
val Maj7 = Maj + M7
val Maj9 = Maj7 + M2
val Maj11 = Maj9 + P4
val Maj13 = Maj11 + M6
val Maj13Sharp11 = Maj9 + M6 + A4
val min = intArrayOf(root, m3, P5)
val min7 = intArrayOf(root, m3, P5, m7)
val min7flat5 = intArrayOf(root, m3, d5, m7)
val min7Add11 = min7 + P4
val min9 = min7 + M2
val min11 = min9 + P4
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
val sus = intArrayOf(root, P4, P5)

// Modern harmonic modes
val Ionian = Maj7 + M2 + P4 + M6
val Dorian = min7 + M2 + P4 + M6
val Phrygian = min7 + m2 + P4 + m6
val Lydian = Maj13Sharp11
val Mixolydian = Dom7 + M2 + P4 + M6
val Aeolian = min7 + M2 + P4 + m6
val Locrian = dim + m2 + P4 + M6 + m7


// Constants used to differentiate between interval properties
val DIMINISHED = 1
val MINOR = 2
val PERFECT = 3
val MAJOR = 4
val AUGMENTED = 5
val NONEXISTENT = 0