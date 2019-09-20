package com.jonlatane.beatpad.midi

import android.media.midi.MidiDevice
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

/**
 * Interface around Android's native MIDI synthesizer support.
 */
@RequiresApi(Build.VERSION_CODES.M)
object MidiSynthesizers: AnkoLogger {
	internal fun setupSynthesizer(info: MidiDeviceInfo, device: MidiDevice): MidiInputPort? {
		return if (info.inputPortCount > 0) {
			val portNumber = info.ports.find {
				it.type == MidiDeviceInfo.PortInfo.TYPE_INPUT
			}!!.portNumber
			device.openInputPort(portNumber)?.let { inputPort ->
				inputPort.send(byteArrayOf(123.toByte()), 0, 1) //All notes off
				MainApplication.instance.toast("Synthesizer ${info.name} connected!")
				inputPort
			}
		} else null
	}

	/**
	 * Basically, skip everything in the Google guide required to reach the
	 * "Sending Play ON" section. Send away! Your signals will go to all
	 * synthesizers or you can specify the one it should go to.
	 */
	internal fun send(data: ByteArray) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			MidiDevices.devices.mapNotNull { it.inputPort }.forEach { port ->
				var error: Throwable? = null
				try {
					port.send(data, 0, data.size)
				} catch (t: Throwable) {
					port.close()
					error("Failed to send midi data", error)
				}
			}
		}
	}
}