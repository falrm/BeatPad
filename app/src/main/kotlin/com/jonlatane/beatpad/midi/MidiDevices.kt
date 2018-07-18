package com.jonlatane.beatpad.midi

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.*
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import com.jonlatane.beatpad.MainApplication
import android.os.HandlerThread
import org.jetbrains.anko.*

object MidiDevices : AnkoLogger {

	@get:RequiresApi(Build.VERSION_CODES.M)
	internal val manager: MidiManager by lazy {
		MainApplication.instance.getSystemService(Context.MIDI_SERVICE) as MidiManager
	}
	internal val handler: Handler by lazy {
		val handlerThread = HandlerThread("MIDIDeviceHandlerThread")
		handlerThread.start()
		val looper = handlerThread.looper
		Handler(looper)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	fun initialize(context: Context) {
		if (MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
			&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
		) {
			val infos = manager.devices
			for (info in infos) {
				setupDevice(info)
			}
			manager.registerDeviceCallback(object : MidiManager.DeviceCallback() {
				@RequiresApi(Build.VERSION_CODES.M)
				override fun onDeviceAdded(info: MidiDeviceInfo) {
					context.toast(
						"Connecting to ${info.properties[MidiDeviceInfo.PROPERTY_NAME]}..."
					)
					setupDevice(info)
				}

				@RequiresApi(Build.VERSION_CODES.M)
				override fun onDeviceRemoved(info: MidiDeviceInfo) {
					context.toast("Disconnected from ${info.name}.")
					MidiSynthesizers.destroySynthesizer(info)
					MidiControllers.destroyController(info)
				}

				override fun onDeviceStatusChanged(status: MidiDeviceStatus) {}
			}, handler)
		}
	}


	@RequiresApi(Build.VERSION_CODES.M)
	private fun setupDevice(info: MidiDeviceInfo) {
		// Again, kinda weirdly, we'll be using input ports to set up output devices
		if (info.inputPortCount > 0) {
			MidiSynthesizers.setupSynthesizer(info)
		} else {
			MainApplication.instance.toast("${info.name} doesn't support MIDI input :(")
		}
		if (info.outputPortCount > 0) {
			MidiControllers.setupController(info)
		} else {
			MainApplication.instance.toast("${info.name} doesn't support MIDI output :(")
		}
	}

	@get:RequiresApi(Build.VERSION_CODES.M)
	internal val MidiDeviceInfo.name: String
		get() {
			return properties[MidiDeviceInfo.PROPERTY_NAME]?.toString() ?: "Unnamed MIDI Device"
		}
}