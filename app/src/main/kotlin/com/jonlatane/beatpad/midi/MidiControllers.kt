package com.jonlatane.beatpad.midi

import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.InputDevice
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.midi.MidiConstants.leftHalf
import com.jonlatane.beatpad.midi.MidiConstants.leftHalfMatchesAny
import com.jonlatane.beatpad.midi.MidiConstants.rightHalf
import com.jonlatane.beatpad.midi.MidiDevices.name
import com.jonlatane.beatpad.util.hexString
//import kotlinx.coroutines.experimental.*
import org.jetbrains.anko.*
import kotlin.experimental.and

object MidiControllers: AnkoLogger {
	// ..and receiving input via devices' output ports
//	val receivers: MutableList<(InputDevice, ByteArray) -> Unit> = mutableListOf()
//	private val inputDevices = mutableMapOf<MidiDeviceInfo, MidiOutputPort>()
//	val controllers get() = inputDevices.keys

	@RequiresApi(Build.VERSION_CODES.M)
	internal fun setupController(info: MidiDeviceInfo, device: MidiDevice): MidiOutputPort? = with(MidiConstants) {
		val portNumber = info.ports.find {
			it.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT
		}!!.portNumber
		device.openOutputPort(portNumber)?.let { outputPort ->
			MainApplication.instance.toast("Controller ${info.name} connected!")
			outputPort.connect(Receiver())
			outputPort
		}
	}

	class Receiver : MidiReceiver() {
		override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
			//info("MIDI data: ${msg.hexString(offset, count)}")
			var byteIndex = offset
			do {
				when {
					msg[byteIndex] == MidiConstants.TICK                                             -> {
						//info("Received beatclock tick ${BeatClockPaletteConsumer.tickPosition}")
						if(AndroidMidi.isPlayingFromExternalDevice) {
							//BeatClockPaletteConsumer.tickPosition++
							//doAsync {
							BeatClockPaletteConsumer.tick()
							//}
						}
					}
					msg[byteIndex] == MidiConstants.PLAY                                             -> {
						//info("Received play")
						BeatClockPaletteConsumer.tickPosition = 0
						AndroidMidi.isPlayingFromExternalDevice = true
					}
					msg[byteIndex] == MidiConstants.STOP                                             -> {
						//info("Received stop")
						AndroidMidi.isPlayingFromExternalDevice = false
						BeatClockPaletteConsumer.tickPosition = 0
					}
					msg[byteIndex] == MidiConstants.SYNC                                             -> {
						//info("Received sync")
						AndroidMidi.lastMidiSyncTime = System.currentTimeMillis()
					}
					msg[byteIndex].leftHalfMatchesAny(MidiConstants.NOTE_ON, MidiConstants.NOTE_OFF) -> {
						//info("Received note on")
						val noteOnOrOff = msg[byteIndex].leftHalf
						val channel =  msg[byteIndex].rightHalf
						val midiTone = msg[++byteIndex]
						val velocity = msg[++byteIndex]
						BeatClockPaletteConsumer.palette?.keyboardPart?.instrument?.let { instrument ->
							when(noteOnOrOff) {
								MidiConstants.NOTE_ON  -> { instrument.play(midiTone.toInt() - 60, velocity.toInt()) }
								MidiConstants.NOTE_OFF -> { instrument.stop(midiTone.toInt() - 60) }
							}
							AndroidMidi.flushSendStream()
						}
					}
					else                                                                             -> {
						error("Unable to parse MIDI: ${msg.hexString(offset, count)}@byte ${byteIndex - offset}")
						return
					}
				}
			} while(++byteIndex < offset + count)
		}
	}
}