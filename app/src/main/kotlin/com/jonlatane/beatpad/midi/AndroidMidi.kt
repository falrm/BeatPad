package com.jonlatane.beatpad.midi

import android.content.pm.PackageManager
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.booleanPref
import opensles.android.fluidsynth.fluidsynth_android_opensles.FluidsynthMidiReceiver
import org.billthefarmer.mididriver.MidiDriver
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable

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
	private var FLUIDSYNTH by observable(FluidsynthMidiReceiver(MainApplication.instance)) { _, old, _ ->
		old.nativeLibJNI.destroy()
	}
	fun resetFluidSynth() {
		FLUIDSYNTH = FluidsynthMidiReceiver(MainApplication.instance)
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
			FLUIDSYNTH.onSend(bytes, 0, bytes.size, System.currentTimeMillis())
		}
		if (
			MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
				&& sendToExternalSynth
		) {
			MidiSynthesizers.send(bytes)
		}
	}

	private fun deactivateUnusedDevices() {
		if(!sendToInternalSynth) {
			ONBOARD_DRIVER.write(byteArrayOf(123.toByte())) // All notes off
			ONBOARD_DRIVER.write(byteArrayOf(0xFF.toByte())) // Midi reset
		}
		if (
			MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
			&& !sendToExternalSynth
		) {
			MidiSynthesizers.send(byteArrayOf(123.toByte())) // All notes off
			MidiSynthesizers.send(byteArrayOf(0xFF.toByte())) // Midi reset
		}
	}
}