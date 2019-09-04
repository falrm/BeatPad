package com.jonlatane.beatpad.midi

import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.InputDevice
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.midi.MidiDevices.name
import com.jonlatane.beatpad.util.hexString
//import kotlinx.coroutines.experimental.*
import org.jetbrains.anko.*
import kotlin.experimental.and

object MidiControllers: AnkoLogger {
	// ..and receiving input via devices' output ports
	val receivers: MutableList<(InputDevice, ByteArray) -> Unit> = mutableListOf()
	private val inputDevices = mutableMapOf<MidiDeviceInfo, MidiOutputPort>()
	val controllers get() = inputDevices.keys

	@RequiresApi(Build.VERSION_CODES.M)
	internal fun setupController(deviceInfo: MidiDeviceInfo) = with(MidiConstants) {
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
								msg[byteIndex] == TICK -> {
									//info("Received beatclock tick ${BeatClockPaletteConsumer.tickPosition}")
									if(AndroidMidi.isPlayingFromExternalDevice) {
										//BeatClockPaletteConsumer.tickPosition++
										//doAsync {
											BeatClockPaletteConsumer.tick()
										//}
									}
								}
								msg[byteIndex] == PLAY                  -> {
									//info("Received play")
									BeatClockPaletteConsumer.tickPosition = 0
									AndroidMidi.isPlayingFromExternalDevice = true
								}
								msg[byteIndex] == STOP                  -> {
									//info("Received stop")
									AndroidMidi.isPlayingFromExternalDevice = false
									BeatClockPaletteConsumer.tickPosition = 0
								}
								msg[byteIndex] == SYNC                  -> {
									//info("Received sync")
									AndroidMidi.lastMidiSyncTime = System.currentTimeMillis()
								}
								msg[byteIndex].leftHalfMatchesAny(NOTE_ON, NOTE_OFF) -> {
									//info("Received note on")
									val noteOnOrOff = msg[byteIndex].leftHalf
									val channel =  msg[byteIndex].rightHalf
									val midiTone = msg[++byteIndex]
									val velocity = msg[++byteIndex]
									BeatClockPaletteConsumer.palette?.keyboardPart?.instrument?.let { instrument ->
										when(noteOnOrOff) {
											NOTE_ON -> { instrument.play(midiTone.toInt() - 60, velocity.toInt()) }
											NOTE_OFF -> { instrument.stop(midiTone.toInt() - 60) }
										}
										AndroidMidi.flushSendStream()
									}
								}
								else                                 -> {
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