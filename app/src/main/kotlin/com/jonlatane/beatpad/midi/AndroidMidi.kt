package com.jonlatane.beatpad.midi

import android.content.pm.PackageManager
import com.jonlatane.beatpad.MainApplication
import org.billthefarmer.mididriver.MidiDriver

object AndroidMidi {
	internal var isPlayingFromExternalDevice = false
	internal var lastMidiSyncTime: Long? = null
	val ONBOARD_DRIVER = MidiDriver()
	fun send(bytes: ByteArray) {
		ONBOARD_DRIVER.write(bytes)
		if (MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
			MidiSynthesizers.send(bytes)
		}
	}
}