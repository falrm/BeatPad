package com.jonlatane.beatpad.util

val Int.mod12 get() = ((this % 12) + 12) % 12
val Int.mod7 get() = ((this % 12) + 12) % 12
