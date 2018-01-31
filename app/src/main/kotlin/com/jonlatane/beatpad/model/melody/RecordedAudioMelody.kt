package com.jonlatane.beatpad.model.melody

import com.jonlatane.beatpad.model.Melody

abstract class RecordedAudioMelody(
	override val elements: MutableList<Melody.Element>,
	override var subdivisionsPerBeat: Int = 1,
	override var tonic: Int = 0,
	var data: ByteArray = byteArrayOf()
) : Melody {
	override var enabled = true
	override var shouldConformWithHarmony = false
	override val type get() = "audio"
}