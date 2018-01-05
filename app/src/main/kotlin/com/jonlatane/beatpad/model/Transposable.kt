package com.jonlatane.beatpad.model

interface Transposable<SelfType: Any> {
	fun transpose(interval: Int): SelfType
}