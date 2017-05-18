package com.jonlatane.beatpad.instrument

import com.leff.midi.MidiFile
import com.leff.midi.MidiTrack
import com.leff.midi.event.MidiEvent
import com.leff.midi.event.ProgramChange

import java.io.File
import java.io.IOException

/**
 * Created by jonlatane on 5/17/17.
 */
class MIDITrackSequencer @Throws(IOException::class)
constructor(file: File, vararg channels: Byte) {
    //internal val instruments: Array<MIDIInstrument>

    private class Event {
        internal var instrument: MIDIInstrument? = null
    }

    /*init {
        instruments = arrayOfNulls<MIDIInstrument>(channels.size)
        for (trackIndex in channels.indices) {
            instruments[trackIndex] = MIDIInstrument()
            instruments[trackIndex].channel = channels[trackIndex]
        }
        loadData(MidiFile(file))
    }*/

    private fun loadData(file: MidiFile) {
        for (trackIndex in 0..file.getTrackCount() - 1) {
            val track = file.getTracks().get(trackIndex)
            for (event in track.getEvents()) {
                if (event is ProgramChange) {
                    //instruments[trackIndex].instrument = (event as ProgramChange).getProgramNumber() as Byte
                }
            }
        }
    }

}
