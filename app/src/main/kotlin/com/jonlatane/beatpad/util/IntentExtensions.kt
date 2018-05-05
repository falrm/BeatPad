package com.jonlatane.beatpad.util

import android.os.Bundle

fun Bundle.formatted(): String {
	var string = "Bundle{"
	this.keySet().forEach { key ->
		string += " " + key + " => " + this.get(key) + ";"
	}
	string += " }"
	return string
}
