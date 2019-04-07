package com.jonlatane.beatpad.model.harmony.chord

//TODO Build just, seriously, a 2048-line lookup table in code. May need to return lists,
//that's cool too.
object ExtensionNameTable {
  fun nameOf(extension: Int): String = when (extension) {
    0b11111111111 -> "Chromatic"
    0b01111111111 -> "Chromatic (no m2)"
    0b10111111111 -> "Chromatic (no 2)"
    0b11011111111 -> "Chromatic (no m3)"
    0b11101111111 -> "Chromatic (no 3)"
    0b11110111111 -> "Chromatic (no 4)"
    0b11111011111 -> "Chromatic (no b5)"
    0b11111101111 -> "Chromatic (no 5)"
    0b11111110111 -> "Chromatic (no m6)"
    0b11111111011 -> "Chromatic (no 6)"
    0b11111111101 -> "Chromatic (no m7)"
    0b11111111110 -> "Chromatic (no M7)"
    0b01011010101 -> "M13(11)"
    0b01010010101 -> "M13"
    0b01011010001 -> "M11"
    0b01010010001 -> "M9"
    0b00010010001 -> "M7"
    0b00010010000 -> "" // Major
    0b00010000000 -> "(no 5)" // Major


    0b01011111111 -> "M13(11)(#11)(b6)(6)(b7)"
    0b10011111111 -> "M13(b9)(11)(#11)(b6)(6)(b7)"
    0b11011111111 -> "Chromatic (no m3)"
    0b11001111111 -> "Chromatic (no 3)"
    0b11010111111 -> "Chromatic (no 4)"
    0b11011011111 -> "Chromatic (no b5)"
    0b11011101111 -> "Chromatic (no 5)"
    0b11011110111 -> "Chromatic (no m6)"
    0b11011111011 -> "Chromatic (no 6)"
    0b11011111101 -> "Chromatic (no m7)"
    0b11011111110 -> "Chromatic (no M7)"


    0b00000010000 -> "5"
    0b00000000000 -> " Alone"
    else -> "TODO"
  }
}