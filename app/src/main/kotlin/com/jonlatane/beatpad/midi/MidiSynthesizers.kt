package com.jonlatane.beatpad.midi

import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.os.Build
import android.support.annotation.RequiresApi
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.midi.MidiDevices.name
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.toast
import org.jetbrains.anko.warn

object MidiSynthesizers: AnkoLogger {
	private const val maxSendRetries = 1000

	// Counterintuitively, we will be outputting via devices' input ports...
	private val outputDevices = mutableMapOf<MidiDeviceInfo, MidiInputPort>()
	val synthesizers get() = outputDevices.keys

	@RequiresApi(Build.VERSION_CODES.M)
	internal fun setupSynthesizer(info: MidiDeviceInfo) {
		val portNumber = info.ports.find {
			it.type == MidiDeviceInfo.PortInfo.TYPE_INPUT
		}!!.portNumber
		MidiDevices.manager.openDevice(info, { device ->
			device?.openInputPort(portNumber)?.let { inputPort ->
				MainApplication.instance.toast("Connected to ${info.name} input!")
				outputDevices[info] = inputPort
			}
		}, MidiDevices.handler)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	internal fun destroySynthesizer(info: MidiDeviceInfo) {
		outputDevices.remove(info)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	private fun sendWithRetry(data: ByteArray, deviceInfo: MidiDeviceInfo) {
		var port =  outputDevices[deviceInfo]!!
		var retries = 0
		var success = false
		var error: Throwable? = null
		do {
			retries++
			try {
				port.send(data, 0, data.size)
				success = true
			} catch (t: Throwable) {
				port.close()
				setupSynthesizer(deviceInfo)
				port = outputDevices[deviceInfo]!!
				error = t
				warn("Failed to send midi data, retrying...", error)
			}
		} while( !success && retries < maxSendRetries)
		if(!success) {
			error("Failed to send midi data", error)
		}
	}

	/**
	 * Basically, skip everything in the Google guide required to reach the
	 * "Sending Play ON" section. Send away! Your signals will go to all
	 * [synthesizers] or you can specify the one it should go to.
	 */
	internal fun send(data: ByteArray, device: MidiDeviceInfo? = null) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (device == null) {
				outputDevices.entries.forEach { (deviceInfo, inputPort) ->
					var port = inputPort
					var retries = 0
					var success = false
					var error: Throwable? = null
					do {
						retries++
						try {
							port.send(data, 0, data.size)
							success = true
						} catch (t: Throwable) {
							port.close()
							setupSynthesizer(deviceInfo)
							port = outputDevices[deviceInfo]!!
							error = t
						}
					} while( !success && retries < maxSendRetries)
					if(!success) {
						error("Failed to send midi data", error)
					}
				}
			} else {
				outputDevices[device]?.send(data, 0, data.size)
			}
		}
	}
}