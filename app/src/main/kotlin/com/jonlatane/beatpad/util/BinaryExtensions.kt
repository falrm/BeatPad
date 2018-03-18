package com.jonlatane.beatpad.util

private val hexArray = "0123456789ABCDEF".toCharArray()

fun ByteArray.asInt(offset: Int = 0, count: Int = size): Int = when (count) {
	0 -> 0
	1 -> get(offset).toInt() and 0xFF
	2 ->
		((get(offset).toInt() and 0xFF) shl 8) or
			(get(offset + 1).toInt() and 0xFF)
	3 ->
		((get(offset).toInt() and 0xF) shl 16) or
			((get(offset + 1).toInt() and 0xFF) shl 8) or
			(get(offset + 2).toInt() and 0xFF)
	4 ->
		((get(offset).toInt() and 0xF) shl 24) or
			((get(offset + 1).toInt() and 0xF) shl 16) or
			((get(offset + 2).toInt() and 0xFF) shl 8) or
			(get(offset + 3).toInt() and 0xFF)
	else -> throw RuntimeException("Too big")
}

fun ByteArray.hexString(offset: Int, count: Int): String {
	val sb = StringBuilder()
	for (b in slice(offset until offset + count)) {
		sb.append(String.format("%02X ", b))
	}
	return sb.toString()
}

val Byte.hexString get() = String.format("%02X ", this)
val Int.hexString get() = String.format("%02X ", this)