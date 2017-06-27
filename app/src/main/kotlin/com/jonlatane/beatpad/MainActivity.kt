package com.jonlatane.beatpad

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.jonlatane.beatpad.harmony.Topology
import com.jonlatane.beatpad.harmony.Topology.*
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.controller.SequencerThread
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.show
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import com.jonlatane.beatpad.view.tempo.TempoTracking
import com.jonlatane.beatpad.view.topology.ANIMATION_DURATION
import com.jonlatane.beatpad.view.topology.RhythmAnimations
import kotlinx.android.synthetic.main.activity_main.*
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.contentView
import org.jetbrains.anko.startActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : BaseActivity() {
	override val menuResource: Int = R.menu.main_menu
	private val playing = AtomicBoolean(false)
	private val executorService = Executors.newScheduledThreadPool(2)
	private lateinit var playingAdvice: Snackbar
	private val pianoBoardInstrument = MIDIInstrument()
	private val harmonicInstrument = MIDIInstrument()
	private val sequencerInstrument = MIDIInstrument()
	private lateinit var keyboardIOHandler: KeyboardIOHandler
	internal val sequencerThread = SequencerThread(sequencerInstrument, 120)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		melody.instrument.channel = 0
		harmonicInstrument.channel = 1
		sequencerInstrument.channel = 2
		pianoBoardInstrument.channel = 3

		melody.instrument.instrument = GeneralMidiConstants.ELECTRIC_PIANO_0
		harmonicInstrument.instrument = GeneralMidiConstants.SYNTHBRASS_1
		sequencerInstrument.instrument = GeneralMidiConstants.STRING_ENSEMBLE_0
		pianoBoardInstrument.instrument = GeneralMidiConstants.SYNTH_BASS_1

		val harmonyController = DeviceOrientationInstrument(harmonicInstrument)
		RhythmAnimations.wireMelodicControl(topology, harmonyController)
		keyboardIOHandler = KeyboardIOHandler(keyboard, pianoBoardInstrument)

		topology.onChordChangedListener = { c: Chord ->
			val tones = c.getTones(-60, 28)
			melody.chord = c
			harmonyController.setTones(tones)
			sequencerThread.setTones(tones)
			keyboardIOHandler.highlightChord(c)
		}

		playingAdvice = Snackbar.make(topology, "Tap the SEQ button again to stop playback.", Snackbar.LENGTH_SHORT)
		sequencerToggle.setOnClickListener(object : View.OnClickListener {
			internal var shown = false
			override fun onClick(v: View) {
				if (!playing.getAndSet(true)) {
					sequencerThread.stopped = false
					executorService.execute(sequencerThread)
					if (!shown) {
						playingAdvice.show()
						shown = true
					}
				} else {
					sequencerThread.stopped = true
					AudioTrackCache.releaseAll()
					playing.set(false)
					playingAdvice.dismiss()
				}
			}
		})
		TempoTracking.trackTempo(tempoTapper) { tempo: Float ->
			Log.i(TAG, "onTempoChanged:" + tempo)
			val bpm = Math.round(tempo)
			if (bpm > 20) {
				sequencerThread.beatsPerMinute = bpm
				updateTempoButton()
			}
		}
		updateTempoButton()
		Orientation.initialize(this)
	}

	override fun onResume() {
		super.onResume()
		topology.onResume()
	}

	override fun onPause() {
		super.onPause()
		sequencerThread.stopped = true
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		val chord = savedInstanceState.getParcelable<Chord>("currentChord")
		if (chord != null) {
			topology.chord = chord
		}
		sequencerThread.beatsPerMinute = savedInstanceState.getInt("tempo")
		updateTempoButton()
		melody.instrument.instrument = savedInstanceState.getByte("melodicInstrument")
		harmonicInstrument.instrument = savedInstanceState.getByte("harmonicInstrument")
		sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument")
		pianoBoardInstrument.instrument = savedInstanceState.getByte("pianoInstrument")
		topology.topology = Topology.values().find { it.ordinal == savedInstanceState.getInt("topologyMode") }!!
		if(savedInstanceState.getBoolean("pianoHidden")) {
			keyboard.hide(animated = false)
			updateTopology()
		}
		if(savedInstanceState.getBoolean("melodyHidden")) {
			melody.hide(animated = false)
			updateTopology()
		}
	}

	// invoked when the activity may be temporarily destroyed, save the instance state here
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putParcelable("currentChord", topology.chord)
		outState.putInt("tempo", sequencerThread.beatsPerMinute)
		outState.putByte("melodicInstrument", melody.instrument.instrument)
		outState.putByte("harmonicInstrument", harmonicInstrument.instrument)
		outState.putByte("sequencerInstrument", sequencerInstrument.instrument)
		outState.putByte("pianoInstrument", pianoBoardInstrument.instrument)
		outState.putBoolean("pianoHidden", keyboard.isHidden)
		outState.putBoolean("melodyHidden", melody.isHidden)
		outState.putInt("topologyMode", topology.topology.ordinal)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.color_board_instrument -> showInstrumentPicker(this, melody.instrument)
			R.id.harmony_instrument -> showInstrumentPicker(this, harmonicInstrument)
			R.id.sequencer_instrument -> showInstrumentPicker(this, sequencerInstrument)
			R.id.piano_board_instrument -> showInstrumentPicker(this, pianoBoardInstrument)
			R.id.choose_tempo -> showTempoPicker(this)
			R.id.keyboard_toggle -> {
				if (keyboard.isHidden) {
					keyboard.show()
					updateTopology()
				} else {
					keyboard.hide()
					updateTopology()
				}
			}
			R.id.color_keyboard_toggle -> {
				if (melody.isHidden) {
					melody.show()
					updateTopology()
				} else {
					melody.hide()
					updateTopology()
				}
			}
			R.id.basic_mode -> topology.topology = basic
			R.id.intermediate_mode -> topology.topology = intermediate
			R.id.advanced_mode -> topology.topology = advanced
			R.id.master_mode -> topology.topology = master
			R.id.chainsmokers_mode -> topology.topology = chainsmokers
			R.id.pop_mode -> topology.topology = pop
			R.id.conduct -> startActivity<ConductorActivity>()
			R.id.play -> startActivity<InstrumentActivity>()
			R.id.sequence_editor -> startActivity<SequenceEditorActivity>()
		}
		return true
	}

	override fun updateMenuOptions() {
		menu.findItem(R.id.piano_board_instrument).title = "Keyboard: ${pianoBoardInstrument.instrumentName}"
		menu.findItem(R.id.color_board_instrument).title = "Colorboard: ${melody.instrument.instrumentName}"
		menu.findItem(R.id.harmony_instrument).title = "Harmony: ${harmonicInstrument.instrumentName}"
		menu.findItem(R.id.sequencer_instrument).title = "Sequencer: ${sequencerInstrument.instrumentName}"
		if(melody.isHidden) {
			menu.findItem(R.id.color_keyboard_toggle).title = "Use Colorboard"
		} else {
			menu.findItem(R.id.color_keyboard_toggle).title = "Hide Colorboard"
		}
		if(keyboard.isHidden) {
			menu.findItem(R.id.keyboard_toggle).title = "Use Keyboard"
		} else {
			menu.findItem(R.id.keyboard_toggle).title = "Hide Keyboard"
		}
		//menu.findItem(R.id.color_keyboard_toggle).title = "${if(melody.isHidden) "Use" else "Hide"} Colorboard"
		//menu.findItem(R.id.keyboard_toggle).title = "${if(keyboard.isHidden) "Use" else "Hide"} Keyboard"
	}

	internal fun updateTempoButton() {
		tempoTapper.text = "${sequencerThread.beatsPerMinute} BPM"
	}

	private fun updateTopology(msDelay: Long = Math.round(1.5 * ANIMATION_DURATION)) {
		executorService.schedule({
			this@MainActivity.contentView?.post {
				updateMenuOptions()
				topology.onResume()
			}
		}, msDelay, TimeUnit.MILLISECONDS)
	}

	companion object {
		private val TAG = MainActivity::class.simpleName
	}
}
