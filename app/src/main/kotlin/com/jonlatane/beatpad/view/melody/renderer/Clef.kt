package com.jonlatane.beatpad.view.melody.renderer

enum class Clef(
  val tones: IntArray
) {
  TREBLE(intArrayOf(4, 7, 11, 14, 17)),
  BASS(intArrayOf(-3, -7, -10, -13, -17))
}