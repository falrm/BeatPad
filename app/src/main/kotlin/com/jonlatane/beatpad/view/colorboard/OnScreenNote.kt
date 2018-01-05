package com.jonlatane.beatpad.view.colorboard

import com.jonlatane.beatpad.sensors.Orientation

internal data class OnScreenNote(
	var tone: Int = 0,
	var pressed: Boolean = false,
	var bottom: Float = 0f,
	var top: Float = 0f
)