package com.jonlatane.beatpad.midi

import org.billthefarmer.mididriver.MidiDriver

object AndroidMidi {
	internal var isPlayingFromExternalDevice = false
	internal var lastMidiSyncTime: Long? = null
	val ONBOARD_DRIVER = MidiDriver()
	fun send(bytes: ByteArray) {
		ONBOARD_DRIVER.write(bytes)
		MidiSynthesizers.send(bytes)
	}
}