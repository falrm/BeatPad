package com.jonlatane.beatpad.midi


object GM2Effects : Map<String, List<GM2Effects.Effect>> by mapOf(
  "Piano" to listOf(
    Effect(0, 0, "Acoustic Grand Piano"),
    Effect(0, 1, "Wide Acoustic Grand"),
    Effect(0, 2, "Dark Acoustic Grand"),
    Effect(1, 0, "Bright Acoustic Piano"),
    Effect(1, 1, "Wide Bright Acoustic"),
    Effect(2, 0, "Electric Grand Piano"),
    Effect(2, 1, "Wide Electric Grand"),
    Effect(3, 0, "Honky-Tonk Piano"),
    Effect(3, 1, "Wide Honky-Tonk"),
    Effect(4, 0, "Rhodes Piano"),
    Effect(4, 1, "Detuned Electric Piano 1"),
    Effect(4, 2, "Electric Piano 1 Variation"),
    Effect(4, 3, "60's Electric Piano"),
    Effect(5, 0, "Chorused Electric Piano"),
    Effect(5, 1, "Detuned Electric Piano 2"),
    Effect(5, 2, "Electric Piano 2 Variation"),
    Effect(5, 3, "Electric Piano Legend"),
    Effect(5, 4, "Electric Piano Phase"),
    Effect(6, 0, "Harpsichord"),
    Effect(6, 1, "Coupled Harpsichord"),
    Effect(6, 2, "Wide Harpsichord"),
    Effect(6, 3, "Open Harpsichord"),
    Effect(7, 0, "Clavinet"),
    Effect(7, 1, "Pulse Clavinet")
  ),
  "Chromatic Percussion" to listOf(
    Effect(8, 0, "Celesta"),
    Effect(9, 0, "Glockenspiel"),
    Effect(10, 0, "Music Box"),
    Effect(11, 0, "Vibraphone"),
    Effect(11, 1, "Wet Vibraphone"),
    Effect(12, 0, "Marimba"),
    Effect(12, 1, "Wide Marimba"),
    Effect(13, 0, "Xylophone"),
    Effect(14, 0, "Tubular Bell"),
    Effect(14, 1, "Church Bell"),
    Effect(14, 2, "Carillon"),
    Effect(15, 0, "Dulcimer/Santur")
  ),
  "Organ" to listOf(
    Effect(16, 0, "Hammond Organ"),
    Effect(16, 1, "Detuned Organ 1"),
    Effect(16, 2, "60's Organ 1"),
    Effect(16, 3, "Organ 4"),
    Effect(17, 0, "Percussive Organ"),
    Effect(17, 1, "Detuned Organ 2"),
    Effect(17, 2, "Organ 5"),
    Effect(18, 0, "Rock Organ"),
    Effect(19, 0, "Church Organ 1"),
    Effect(19, 1, "Church Organ 2"),
    Effect(19, 2, "Church Organ 3"),
    Effect(20, 0, "Reed Organ"),
    Effect(20, 1, "Puff Organ"),
    Effect(21, 0, "French Accordion"),
    Effect(21, 1, "Italian Accordion"),
    Effect(22, 0, "Harmonica"),
    Effect(23, 0, "Bandoneon")
  ),
  "Guitar" to listOf(
    Effect(24, 0, "Nylon-String Guitar"),
    Effect(24, 1, "Ukulele"),
    Effect(24, 2, "Open Nylon Guitar"),
    Effect(24, 3, "Nylon Guitar 2"),
    Effect(25, 0, "Steel-String Guitar"),
    Effect(25, 1, "12-String Guitar"),
    Effect(25, 2, "Mandolin"),
    Effect(25, 3, "Steel + Body"),
    Effect(26, 0, "Jazz Guitar"),
    Effect(26, 1, "Hawaiian Guitar"),
    Effect(27, 0, "Clean Electric Guitar"),
    Effect(27, 1, "Chorus Guitar"),
    Effect(27, 2, "Mid Tone Guitar"),
    Effect(28, 0, "Muted Electric Guitar"),
    Effect(28, 1, "Funk Guitar"),
    Effect(28, 2, "Funk Guitar 2"),
    Effect(28, 3, "Jazz Man"),
    Effect(29, 0, "Overdriven Guitar"),
    Effect(29, 1, "Guitar Pinch"),
    Effect(30, 0, "Distortion Guitar"),
    Effect(30, 1, "Feedback Guitar"),
    Effect(30, 2, "Distortion Rtm Guitar"),
    Effect(31, 0, "Guitar Harmonics"),
    Effect(31, 1, "Guitar Feedback")
  ),
  "Bass" to listOf(
    Effect(32, 0, "Acoustic Bass"),
    Effect(33, 0, "Fingered Bass"),
    Effect(33, 1, "Finger Slap"),
    Effect(34, 0, "Picked Bass"),
    Effect(35, 0, "Fretless Bass"),
    Effect(36, 0, "Slap Bass 1"),
    Effect(37, 0, "Slap Bass 2"),
    Effect(38, 0, "Synth Bass 1"),
    Effect(38, 1, "Synth Bass 101"),
    Effect(38, 2, "Synth Bass 3"),
    Effect(38, 3, "Clavi Bass"),
    Effect(38, 4, "Hammer"),
    Effect(39, 0, "Synth Bass 2"),
    Effect(39, 1, "Synth Bass 4"),
    Effect(39, 2, "Rubber Bass"),
    Effect(39, 3, "Attack Pulse")
  ),
  "Orchestra Solo" to listOf(
    Effect(40, 0, "Violin"),
    Effect(40, 1, "Slow Violin"),
    Effect(41, 0, "Viola"),
    Effect(42, 0, "Cello"),
    Effect(43, 0, "Contrabass"),
    Effect(44, 0, "Tremolo Strings"),
    Effect(45, 0, "Pizzicato Strings"),
    Effect(46, 0, "Harp"),
    Effect(46, 1, "Yang Qin"),
    Effect(47, 0, "Timpani")
  ),
  "Orchestra Ensemble" to listOf(
    Effect(48, 0, "Orchestra Ensemble: TODO")
  ),
  "Brass" to listOf(
    Effect(56, 0, "Brass: TODO")
  ),
  "Reed" to listOf(
    Effect(64, 0, "Reed: TODO")
  ),
  "Wind" to listOf(
    Effect(72, 0, "Wind: TODO")
  ),
  "Synth Lead" to listOf(
    Effect(80, 0, "Synth Lead: TODO")
  ),
  "Synth Pad" to listOf(
    Effect(88, 0, "Synth Pad: TODO")
  ),
  "Synth Sound FX" to listOf(
    Effect(96, 0, "Synth Sound FX: TODO")
  ),
  "Ethnic" to listOf(
    Effect(104, 0, "Ethnic: TODO")
  ),
  "Percussive" to listOf(
    Effect(112, 0, "Percussive: TODO")
  ),
  "Sound Effect" to listOf(
    Effect(120, 0, "Sound Effects: TODO")
  )
) {
  val all: List<Effect> = values.flatMap { it }
  fun find(patchNumber: Int, bankNumber: Int): Effect? = all.find {
    it.patchNumber == patchNumber && it.bankNumber == bankNumber
  }
  class Effect(
    val patchNumber: Int,
    val bankNumber: Int,
    val patchName: String
  )
}