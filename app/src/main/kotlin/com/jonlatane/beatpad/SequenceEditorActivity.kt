package com.jonlatane.beatpad

import android.app.Activity
import android.os.Bundle
import com.jonlatane.beatpad.harmony.Orbifold
import com.jonlatane.beatpad.harmony.Orbifold.intermediate
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.storage.ToneSequenceStorage
import com.jonlatane.beatpad.view.tonesequence.ToneSequencePlayerThread
import com.jonlatane.beatpad.view.tonesequence.ToneSequenceUI
import org.billthefarmer.mididriver.GeneralMidiConstants.*
import org.jetbrains.anko.*

class SequenceEditorActivity : Activity(), AnkoLogger {
	lateinit var ui: ToneSequenceUI
	val viewModel get() = ui.viewModel
	val sequencerInstrument get() = ui.sequencerInstrument
	val orbifold get() = viewModel.orbifold
	var toneSequence get() = viewModel.toneSequence
		set(value) {
			viewModel.toneSequence = value
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		info("hi hi hi hi")
		ui = ToneSequenceUI().also {
			it.setContentView(this)
		}
		val bundle = savedInstanceState ?: intent.extras.getBundle("playgroundState")

		fun Bundle.formatted(): String {
			var string = "Bundle{"
			this.keySet().forEach { key ->
				string += " " + key + " => " + this.get(key) + ";"
			}
			string += " }"
			return string
		}

		println("Got intent with extras: ${bundle.formatted()}")
		onRestoreInstanceState(bundle)
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
		ToneSequenceStorage.storeSequence(toneSequence, this)
	}

	override fun onStop() {
		super.onStop()
		ToneSequenceStorage.storeSequence(toneSequence, this)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		toneSequence = ToneSequenceStorage.loadSequence(this)
		ui.sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument", SYNTH_BASS_1)
		orbifold.orbifold = Orbifold.values().find {
			it.ordinal == (savedInstanceState["orbifoldMode"] as Int? ?: -1)
		} ?: intermediate
		val chord = savedInstanceState.getParcelable<Chord>("currentChord")
		if (chord != null) {
			orbifold.chord = chord
		}
		viewModel.sequencerThread =  ToneSequencePlayerThread(sequencerInstrument, viewModel, beatsPerMinute = 104)
		ui.sequencerThread.beatsPerMinute = savedInstanceState.getInt("tempo", 147)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelable("currentChord", orbifold.chord)
		outState.putInt("tempo", ui.sequencerThread.beatsPerMinute)
		outState.putByte("sequencerInstrument", ui.sequencerInstrument.instrument)
		outState.putInt("orbifoldMode", orbifold.orbifold.ordinal)
	}
}