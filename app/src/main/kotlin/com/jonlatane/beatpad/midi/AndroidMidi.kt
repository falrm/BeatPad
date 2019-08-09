package com.jonlatane.beatpad.midi

import android.content.pm.PackageManager
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.booleanPref
import org.billthefarmer.mididriver.MidiDriver

/**
 * Singleton interface to native MIDI android devices (via [PackageManager.FEATURE_MIDI]).
 */
object AndroidMidi {
	internal var isPlayingFromExternalDevice = false
	internal var lastMidiSyncTime: Long? = null
	val ONBOARD_DRIVER = MidiDriver()
	private var sendToInternalSynthSetting by booleanPref("sendToInternalSynth", true)
	private var sendToExternalSynthSetting by booleanPref("sendToExternalSynth", true)

	var sendToExternalSynth = sendToExternalSynthSetting
		set(value) {
			field = value
			sendToExternalSynthSetting = value
		}
	var sendToInternalSynth = sendToInternalSynthSetting
		set(value) {
			field = value
			sendToInternalSynthSetting = value
		}
	fun send(bytes: ByteArray) {
		if(sendToInternalSynth) {
			ONBOARD_DRIVER.write(bytes)
		}
		if (
			MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
				&& sendToExternalSynth
		) {
			MidiSynthesizers.send(bytes)
		}
	}
}