package com.jonlatane.beatpad.util

val Int.mod12 get() = ((this % 12) + 12) % 12
val Int.mod7 get() = ((this % 12) + 12) % 12
val Int.fromOctaveToOffset get() = (this - 4) * 12
val Float.to255Int
	get() = Math.min(255, Math.max(0, Math.round(this * 255)))
val Float.to127Int
	get() = Math.min(127, Math.max(0, Math.round(this * 127)))

fun gcd(a: Int, b: Int): Int {
	if (b == 0) return a
	return gcd(b, a % b)
}

fun lcm(a: Int, b: Int): Int {
	return a / gcd(a, b) * b
}