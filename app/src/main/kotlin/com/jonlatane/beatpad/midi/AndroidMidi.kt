package com.jonlatane.beatpad.midi

import android.content.pm.PackageManager
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.booleanPref
import fluidsynth.FluidSynthMidiReceiver
import org.billthefarmer.mididriver.MidiDriver
import org.jetbrains.anko.AnkoLogger
import java.io.ByteArrayOutputStream

/**
 * Singleton interface to both the Sonivox synthesizer ([ONBOARD_DRIVER])
 * and native MIDI android devices (via [PackageManager.FEATURE_MIDI]).
 */
object AndroidMidi : AnkoLogger {
	internal var isPlayingFromExternalDevice = false
	internal var lastMidiSyncTime: Long? = null
	val ONBOARD_DRIVER = MidiDriver()
	init {
		System.loadLibrary("fluidsynthjni")
	}
	private var FLUIDSYNTH = FluidSynthMidiReceiver(MainApplication.instance)
	fun resetFluidSynth() {
		FLUIDSYNTH.nativeLibJNI.destroy()
		FLUIDSYNTH = FluidSynthMidiReceiver(MainApplication.instance)
		MidiDevices.refreshInstruments()
	}
	private var sendToInternalSynthSetting by booleanPref("sendToInternalSynth", false)
	private var sendToInternalFluidSynthSetting by booleanPref("sendToInternalFluidSynth", true)
	private var sendToExternalSynthSetting by booleanPref("sendToExternalSynth", false)

	var sendToExternalSynth = sendToExternalSynthSetting
		set(value) {
			field = value
			sendToExternalSynthSetting = value
			if(!value) deactivateUnusedDevices()
		}
	var sendToInternalSynth = sendToInternalSynthSetting
		set(value) {
			field = value
			sendToInternalSynthSetting = value
			if(!value) deactivateUnusedDevices()
		}
	var sendToInternalFluidSynth = sendToInternalFluidSynthSetting
		set(value) {
			field = value
			sendToInternalFluidSynthSetting = value
			if(!value) deactivateUnusedDevices()
		}

	val sendStream = ByteArrayOutputStream(2048)
	fun flushSendStream() {
		send (
			synchronized(sendStream) {
				sendStream.toByteArray().copyOf().also {
					sendStream.reset()
				}
			}
		)
	}
	fun send(bytes: ByteArray) {
		if(sendToInternalSynth) {
			ONBOARD_DRIVER.write(bytes)
		}
		if(sendToInternalFluidSynth) {
			FLUIDSYNTH.send(bytes, 0, bytes.size, System.currentTimeMillis())
		}
		if (
			MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
				&& sendToExternalSynth
		) {
			MidiSynthesizers.send(bytes)
		}
	}

	private fun deactivateUnusedDevices() {
		if(!sendToInternalFluidSynth) {
			stopMidiReceiver { FLUIDSYNTH.send(it, 0, it.size) }

		}
		if(!sendToInternalSynth) {
			stopMidiReceiver { ONBOARD_DRIVER.write(it) }
		}
		if (
			MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
			&& !sendToExternalSynth
		) {
			stopMidiReceiver { MidiSynthesizers.send(it) }
		}
	}

	fun stopMidiReceiver(send: (ByteArray) -> Unit) {
		(0 until 16).forEach {  channel ->
			send(byteArrayOf(((0b1011 shl 4) + channel).toByte(), 123, 0)) // All notes off
			send(byteArrayOf(((0b1011 shl 4) + channel).toByte(), 120, 0)) // All sound off
		}
	}
}