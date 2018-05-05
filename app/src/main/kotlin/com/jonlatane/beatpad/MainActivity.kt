package com.jonlatane.beatpad

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.model.harmony.Orbifold.*
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.storage.MelodyStorage
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.show
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import com.jonlatane.beatpad.view.tempo.TempoTracking
import com.jonlatane.beatpad.output.controller.ToneSequencePlayerThread
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.view.orbifold.ANIMATION_DURATION
import com.jonlatane.beatpad.view.orbifold.RhythmAnimations
import kotlinx.android.synthetic.main.activity_main.*
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.contentView
import org.jetbrains.anko.startActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : OldBaseActivity() {
	override val menuResource: Int = R.menu.main_menu
	private val playing = AtomicBoolean(false)
	private val executorService = Executors.newScheduledThreadPool(2)
	private val pianoBoardInstrument get() = keyboardIOHandler.instrument
	private val harmonicInstrument = MIDIInstrument()
	private val sequencerInstrument = MIDIInstrument()
	private lateinit var keyboardIOHandler: KeyboardIOHandler
	internal lateinit var sequencerThread: ToneSequencePlayerThread
	internal lateinit var asdf: Melody

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		keyboardIOHandler = KeyboardIOHandler(keyboard)

		(colorboard.instrument as? MIDIInstrument)?.apply {
			channel = 0
			instrument = GeneralMidiConstants.ROCK_ORGAN
		}
		harmonicInstrument.channel = 1
		harmonicInstrument.instrument = GeneralMidiConstants.SYNTHBRASS_1

		sequencerInstrument.channel = 2
		sequencerInstrument.instrument = GeneralMidiConstants.ACOUSTIC_GRAND_PIANO

		(pianoBoardInstrument as? MIDIInstrument)?.apply {
			channel = 3
			instrument = GeneralMidiConstants.SYNTH_BASS_1
		}

		supportActionBar?.elevation = 0f

		val harmonyController = DeviceOrientationInstrument(harmonicInstrument)
		RhythmAnimations.wireMelodicControl(orbifold, harmonyController)
    asdf = Storage.loadSequence(this)

		sequencerThread = ToneSequencePlayerThread(
			sequencerInstrument,
			sequence = asdf,
			beatsPerMinute = 120,
			chordResolver = { orbifold.chord }
		)

		orbifold.onChordChangedListener = { c: Chord ->
			val tones = c.getTones()
			colorboard.chord = c
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
	}

	override fun onResume() {
		super.onResume()
		orbifold.onResume()
		asdf = Storage.loadSequence(this)
	}

	override fun onPause() {
		super.onPause()
		sequencerThread.stopped = true
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		val chord = savedInstanceState.getParcelable<Chord>("currentChord")
		if (chord != null) {
			orbifold.chord = chord
		}
		sequencerThread.beatsPerMinute = savedInstanceState.getInt("tempo")
		updateTempoButton()
		(colorboard.instrument as? MIDIInstrument)?.instrument = savedInstanceState.getByte("melodicInstrument")
		harmonicInstrument.instrument = savedInstanceState.getByte("harmonicInstrument")
		sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument")
		(pianoBoardInstrument as? MIDIInstrument)?.instrument = savedInstanceState.getByte("pianoInstrument")
		orbifold.orbifold = Orbifold.values().find { it.ordinal == savedInstanceState.getInt("orbifoldMode") }!!
		if (savedInstanceState.getBoolean("pianoHidden")) {
			keyboard.hide(animated = false)
			updateOrbifold()
		}
		if (savedInstanceState.getBoolean("melodyHidden")) {
			colorboard.hide(animated = false)
			updateOrbifold()
		}
	}

	// invoked when the activity may be temporarily destroyed, save the instance state here
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelable("currentChord", orbifold.chord)
		outState.putInt("tempo", sequencerThread.beatsPerMinute)
		outState.putByte("melodicInstrument", (colorboard.instrument as? MIDIInstrument)?.instrument ?: 0)
		outState.putByte("harmonicInstrument", harmonicInstrument.instrument)
		outState.putByte("sequencerInstrument", sequencerInstrument.instrument)
		outState.putByte("pianoInstrument", (pianoBoardInstrument as? MIDIInstrument)?.instrument ?: 0)
		outState.putBoolean("pianoHidden", keyboard.isHidden)
		outState.putBoolean("melodyHidden", colorboard.isHidden)
		outState.putInt("orbifoldMode", orbifold.orbifold.ordinal)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.color_board_instrument -> (colorboard.instrument as? MIDIInstrument)?.let {showInstrumentPicker(it, this) }
			R.id.harmony_instrument -> showInstrumentPicker(harmonicInstrument, this)
			R.id.sequencer_instrument -> showInstrumentPicker(sequencerInstrument, this)
			R.id.piano_board_instrument -> (pianoBoardInstrument as? MIDIInstrument)?.let {showInstrumentPicker(it, this) }
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
				if (colorboard.isHidden) {
					colorboard.show()
					updateOrbifold()
				} else {
					colorboard.hide()
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
			R.id.sequence_editor -> startActivity<SequenceEditorActivity>()
			R.id.palette_editor -> startActivity<PaletteEditorActivity>()
		}
		return true
	}

	override fun updateMenuOptions() {
		menu.findItem(R.id.piano_board_instrument).title = "Keyboard: ${pianoBoardInstrument.instrumentName}"
		menu.findItem(R.id.color_board_instrument).title = "Colorboard: ${colorboard.instrument.instrumentName}"
		menu.findItem(R.id.harmony_instrument).title = "Harmony: ${harmonicInstrument.instrumentName}"
		menu.findItem(R.id.sequencer_instrument).title = "Sequencer: ${sequencerInstrument.instrumentName}"
		if (colorboard.isHidden) {
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
