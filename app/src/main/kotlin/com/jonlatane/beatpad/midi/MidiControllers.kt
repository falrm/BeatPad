package com.jonlatane.beatpad.midi

import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.InputDevice
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.midi.MidiDevices.name
import com.jonlatane.beatpad.util.asInt
import com.jonlatane.beatpad.util.hexString
//import kotlinx.coroutines.experimental.*
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.async
import java.util.concurrent.ScheduledExecutorService
import kotlin.experimental.and

object MidiControllers: AnkoLogger {
	// ..and receiving input via devices' output ports
	val receivers: MutableList<(InputDevice, ByteArray) -> Unit> = mutableListOf()
	private val inputDevices = mutableMapOf<MidiDeviceInfo, MidiOutputPort>()
	val controllers get() = inputDevices.keys
	const val TICK = 0xF8.toByte()
	const val PLAY = 0xFA.toByte()
	const val STOP = 0xFC.toByte()
	const val SYNC = 0xFE.toByte()
	private const val LEFT_MASK = 0xF0
	fun Byte.firstHalfIs(value: Int) = (this.toInt() and LEFT_MASK) == (value shl 4)
	const val NOTE_ON = 9

	@RequiresApi(Build.VERSION_CODES.M)
	internal fun setupController(deviceInfo: MidiDeviceInfo) {
		val portNumber = deviceInfo.ports.find {
			it.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT
		}!!.portNumber
		MidiDevices.manager.openDevice(deviceInfo, { device ->
			device?.openOutputPort(portNumber)?.let { outputPort ->
				MainApplication.instance.toast("Connected to ${deviceInfo.name} output!")
				inputDevices[deviceInfo] = outputPort
				outputPort.connect(object : MidiReceiver() {
					override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
						//info("MIDI data: ${msg.hexString(offset, count)}")
						var byteIndex = offset
						do {
							when {
								msg[offset] == TICK -> {
									//info("Received beatclock tick ${BeatClockPaletteConsumer.tickPosition}")
									if(AndroidMidi.isPlayingFromExternalDevice) {
										//BeatClockPaletteConsumer.tickPosition++
										//doAsync {
											BeatClockPaletteConsumer.tick()
										//}
									}
								}
								msg[offset] == PLAY -> {
									//info("Received play")
									BeatClockPaletteConsumer.tickPosition = 0
									AndroidMidi.isPlayingFromExternalDevice = true
								}
								msg[offset] == STOP -> {
									//info("Received stop")
									AndroidMidi.isPlayingFromExternalDevice = false
									BeatClockPaletteConsumer.tickPosition = 0
								}
								msg[offset] == SYNC -> {
									//info("Received sync")
									AndroidMidi.lastMidiSyncTime = System.currentTimeMillis()
								}
								msg[offset].firstHalfIs(NOTE_ON) -> {
									//info("Received note on")
									byteIndex += 2
								}
								else -> {
									error("Unable to parse MIDI: ${msg.hexString(offset, count)}@byte ${byteIndex - offset}")
									return
								}
							}
						} while(++byteIndex < offset + count)
					}
				})
			}
		}, MidiDevices.handler)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	internal fun destroyController(info: MidiDeviceInfo) {
		inputDevices.remove(info)
	}
}