package com.jonlatane.beatpad

import android.app.Activity
import android.os.Bundle
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.util.formatted
import com.jonlatane.beatpad.view.palette.PaletteUI
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.setContentView

class PaletteEditorActivity : Activity(), AnkoLogger {
	lateinit var ui: PaletteUI
	val viewModel get() = ui.viewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		ui = PaletteUI().also {
			it.setContentView(this)
		}
		viewModel.palette = PaletteStorage.loadPalette(this)

		val bundle = savedInstanceState ?: try {
			intent.extras.getBundle("playgroundState")
		} catch (t: Throwable) {
			savedInstanceState
		}

		if (bundle != null) {
			println("Got intent with extras: ${bundle.formatted()}")
			onRestoreInstanceState(bundle)
		}
	}

	override fun onBackPressed() {
		if (!viewModel.onBackPressed()) super.onBackPressed()
	}

	override fun onResume() {
		super.onResume()
		//try {
			AndroidMidi.ONBOARD_DRIVER.start()
		//} catch( t: Throwable) { error(t) }

		// Get the configuration.
		val config = AndroidMidi.ONBOARD_DRIVER.config()

		// Print out the details.
		debug("maxVoices: " + config[0])
		debug("numChannels: " + config[1])
		debug("sampleRate: " + config[2])
		debug("mixBufferSize: " + config[3])
	}

	override fun onPause() {
		super.onPause()
		AudioTrackCache.releaseAll()
		AndroidMidi.ONBOARD_DRIVER.stop()
		//ui.sequencerThread.stopped = true
		PaletteStorage.storePalette(viewModel.palette, this)
	}

	override fun onStop() {
		super.onStop()
		PaletteStorage.storePalette(viewModel.palette, this)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		ui.sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument", GeneralMidiConstants.SYNTH_BASS_1)
		viewModel.orbifold.orbifold = Orbifold.values().find {
			it.ordinal == (savedInstanceState["orbifoldMode"] as Int? ?: -1)
		} ?: Orbifold.intermediate
		val chord = savedInstanceState.getParcelable<Chord>("currentChord")
		if (chord != null) {
			viewModel.orbifold.chord = chord
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		PaletteStorage.storePalette(viewModel.palette, this)
		outState.putParcelable("currentChord", viewModel.orbifold.chord)
		//outState.putInt("tempo", ui.sequencerThread.beatsPerMinute)
		//outState.putByte("sequencerInstrument", ui.sequencerInstrument.instrument)
		outState.putInt("orbifoldMode", viewModel.orbifold.orbifold.ordinal)
	}
}