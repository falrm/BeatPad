package com.jonlatane.beatpad

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.jonlatane.beatpad.model.ToneSequence
import com.jonlatane.beatpad.harmony.Orbifold
import com.jonlatane.beatpad.harmony.Orbifold.*
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.storage.ToneSequenceStorage
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.show
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import com.jonlatane.beatpad.view.tempo.TempoTracking
import com.jonlatane.beatpad.output.controller.ToneSequencePlayerThread
import com.jonlatane.beatpad.view.orbifold.ANIMATION_DURATION
import com.jonlatane.beatpad.view.orbifold.RhythmAnimations
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
	private val pianoBoardInstrument = MIDIInstrument()
	private val harmonicInstrument = MIDIInstrument()
	private val sequencerInstrument = MIDIInstrument()
	private lateinit var keyboardIOHandler: KeyboardIOHandler
	internal lateinit var sequencerThread: ToneSequencePlayerThread
	internal lateinit var toneSequence: ToneSequence

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		melody.instrument.channel = 0
		harmonicInstrument.channel = 1
		sequencerInstrument.channel = 2
		pianoBoardInstrument.channel = 3

		supportActionBar?.elevation = 0f

		melody.instrument.instrument = GeneralMidiConstants.ROCK_ORGAN
		harmonicInstrument.instrument = GeneralMidiConstants.SYNTHBRASS_1
		sequencerInstrument.instrument = GeneralMidiConstants.ACOUSTIC_GRAND_PIANO
		pianoBoardInstrument.instrument = GeneralMidiConstants.SYNTH_BASS_1

		val harmonyController = DeviceOrientationInstrument(harmonicInstrument)
		RhythmAnimations.wireMelodicControl(orbifold, harmonyController)
		keyboardIOHandler = KeyboardIOHandler(keyboard, pianoBoardInstrument)
        toneSequence = ToneSequenceStorage.loadSequence(this)

		sequencerThread = ToneSequencePlayerThread(
			sequencerInstrument,
			sequence = toneSequence,
			beatsPerMinute = 120,
			chordResolver = { orbifold.chord }
		)

		orbifold.onChordChangedListener = { c: Chord ->
			val tones = c.getTones()
			melody.chord = c
			harmonyController.tones = tones
			keyboardIOHandler.highlightChord(c)
		}

		sequencerToggle.setOnClickListener {
			if (!playing.getAndSet(true)) {
				sequencerThread.stopped = false
				executorService.execute(sequencerThread)
				sequencerToggle.text = "Stop"
			} else {
				sequencerThread.stopped = true
				AudioTrackCache.releaseAll()
				playing.set(false)
				sequencerToggle.text = "Seq"
			}
		}
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
		orbifold.onResume()
		toneSequence = ToneSequenceStorage.loadSequence(this)
	}

	override fun onPause() {
		super.onPause()
		sequencerThread.stopped = true
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		val chord = savedInstanceState.getParcelable<Chord>("currentChord")
		if (chord != null) {
			orbifold.chord = chord
		}
		sequencerThread.beatsPerMinute = savedInstanceState.getInt("tempo")
		updateTempoButton()
		melody.instrument.instrument = savedInstanceState.getByte("melodicInstrument")
		harmonicInstrument.instrument = savedInstanceState.getByte("harmonicInstrument")
		sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument")
		pianoBoardInstrument.instrument = savedInstanceState.getByte("pianoInstrument")
		orbifold.orbifold = Orbifold.values().find { it.ordinal == savedInstanceState.getInt("orbifoldMode") }!!
		if (savedInstanceState.getBoolean("pianoHidden")) {
			keyboard.hide(animated = false)
			updateOrbifold()
		}
		if (savedInstanceState.getBoolean("melodyHidden")) {
			melody.hide(animated = false)
			updateOrbifold()
		}
	}

	// invoked when the activity may be temporarily destroyed, save the instance state here
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putParcelable("currentChord", orbifold.chord)
		outState.putInt("tempo", sequencerThread.beatsPerMinute)
		outState.putByte("melodicInstrument", melody.instrument.instrument)
		outState.putByte("harmonicInstrument", harmonicInstrument.instrument)
		outState.putByte("sequencerInstrument", sequencerInstrument.instrument)
		outState.putByte("pianoInstrument", pianoBoardInstrument.instrument)
		outState.putBoolean("pianoHidden", keyboard.isHidden)
		outState.putBoolean("melodyHidden", melody.isHidden)
		outState.putInt("orbifoldMode", orbifold.orbifold.ordinal)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.color_board_instrument -> showInstrumentPicker(melody.instrument, this)
			R.id.harmony_instrument -> showInstrumentPicker(harmonicInstrument, this)
			R.id.sequencer_instrument -> showInstrumentPicker(sequencerInstrument, this)
			R.id.piano_board_instrument -> showInstrumentPicker(pianoBoardInstrument, this)
			R.id.choose_tempo -> showTempoPicker(this)
			R.id.keyboard_toggle -> {
				if (keyboard.isHidden) {
					keyboard.show()
					updateOrbifold()
				} else {
					keyboard.hide()
					updateOrbifold()
				}
			}
			R.id.color_keyboard_toggle -> {
				if (melody.isHidden) {
					melody.show()
					updateOrbifold()
				} else {
					melody.hide()
					updateOrbifold()
				}
			}
			R.id.basic_mode -> orbifold.orbifold = basic
			R.id.intermediate_mode -> orbifold.orbifold = intermediate
			R.id.advanced_mode -> orbifold.orbifold = advanced
			R.id.master_mode -> orbifold.orbifold = master
			R.id.chainsmokers_mode -> orbifold.orbifold = chainsmokers
			R.id.pop_mode -> orbifold.orbifold = pop
			R.id.conduct -> startActivity<ConductorActivity>()
			R.id.play -> startActivity<InstrumentActivity>()
			R.id.sequence_editor -> startActivity<SequenceEditorActivity>("playgroundState" to Bundle().also { onSaveInstanceState(it) })
			R.id.palette_editor -> startActivity<PaletteEditorActivity>("playgroundState" to Bundle().also { onSaveInstanceState(it) })
		}
		return true
	}

	override fun updateMenuOptions() {
		menu.findItem(R.id.piano_board_instrument).title = "Keyboard: ${pianoBoardInstrument.instrumentName}"
		menu.findItem(R.id.color_board_instrument).title = "Colorboard: ${melody.instrument.instrumentName}"
		menu.findItem(R.id.harmony_instrument).title = "Harmony: ${harmonicInstrument.instrumentName}"
		menu.findItem(R.id.sequencer_instrument).title = "Sequencer: ${sequencerInstrument.instrumentName}"
		if (melody.isHidden) {
			menu.findItem(R.id.color_keyboard_toggle).title = "Use Colorboard"
		} else {
			menu.findItem(R.id.color_keyboard_toggle).title = "Hide Colorboard"
		}
		if (keyboard.isHidden) {
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

	private fun updateOrbifold(msDelay: Long = Math.round(1.5 * ANIMATION_DURATION)) {
		executorService.schedule({
			this@MainActivity.contentView?.post {
				updateMenuOptions()
				orbifold.onResume()
			}
		}, msDelay, TimeUnit.MILLISECONDS)
	}

	companion object {
		private val TAG = MainActivity::class.simpleName
	}
}
