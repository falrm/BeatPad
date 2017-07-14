package com.jonlatane.beatpad

import android.app.Activity
import android.os.Bundle
import com.jonlatane.beatpad.harmony.Topology
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.tonesequence.ToneSequenceUI
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.setContentView

class SequenceEditorActivity : Activity(), AnkoLogger {
	lateinit var ui: ToneSequenceUI
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		info("hi hi hi hi")
		ui = ToneSequenceUI().also {
			it.setContentView(this)
		}
	}

	override fun onResume() {
		super.onResume()
		MIDIInstrument.DRIVER.start()

		// Get the configuration.
		val config = MIDIInstrument.DRIVER.config()

		// Print out the details.
		debug("maxVoices: " + config[0])
		debug("numChannels: " + config[1])
		debug("sampleRate: " + config[2])
		debug("mixBufferSize: " + config[3])
	}

	override fun onPause() {
		super.onPause()
		AudioTrackCache.releaseAll()
		MIDIInstrument.DRIVER.stop()
		ui.sequencerThread.stopped = true
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		val chord = savedInstanceState.getParcelable<Chord>("currentChord")
		if (chord != null) {
			topology.chord = chord
		}
		ui.sequencerThread.beatsPerMinute = savedInstanceState.getInt("tempo")
		//updateTempoButton()
		melody.instrument.instrument = savedInstanceState.getByte("melodicInstrument")
		//harmonicInstrument.instrument = savedInstanceState.getByte("harmonicInstrument")
		ui.sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument")
		//pianoBoardInstrument.instrument = savedInstanceState.getByte("pianoInstrument")
		topology.topology = Topology.values().find { it.ordinal == savedInstanceState.getInt("topologyMode") }!!
		if (savedInstanceState.getBoolean("pianoHidden")) {
			//keyboard.hide(animated = false)
			//updateTopology()
		}
		if (savedInstanceState.getBoolean("melodyHidden")) {
			//melody.hide(animated = false)
			//updateTopology()
		}
	}

	// invoked when the activity may be temporarily destroyed, save the instance state here
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putParcelable("currentChord", topology.chord)
		outState.putInt("tempo", ui.sequencerThread.beatsPerMinute)
		outState.putByte("melodicInstrument", melody.instrument.instrument)
		//outState.putByte("harmonicInstrument", harmonicInstrument.instrument)
		outState.putByte("sequencerInstrument", ui.sequencerInstrument.instrument)
		//outState.putByte("pianoInstrument", pianoBoardInstrument.instrument)
		//outState.putByte("pianoInstrument", pianoBoardInstrument.instrument)
		outState.putBoolean("pianoHidden", keyboard.isHidden)
		outState.putBoolean("melodyHidden", melody.isHidden)
		outState.putInt("topologyMode", topology.topology.ordinal)
	}
}