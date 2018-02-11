package com.jonlatane.beatpad.view.midi

import com.jonlatane.beatpad.util.booleanPref

object MidiFlowControl {
	var midiNoteInputEnabled by booleanPref("midiNoteInput")
	var midiNoteOutputEnabled by booleanPref("midiNoteOutput")
	var midiNotePassthroughEnabled by booleanPref("midiNotePassthrough")

	var midiClockInputEnabled by booleanPref("midiClockInput")
	var midiClockOutputEnabled by booleanPref("midiClockOutput")
	var midiClockPassthroughEnabled by booleanPref("midiClockPassthrough")
}