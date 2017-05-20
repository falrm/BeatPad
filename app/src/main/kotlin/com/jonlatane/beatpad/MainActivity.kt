package com.jonlatane.beatpad

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.jonlatane.beatpad.audio.AudioTrackCache
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument
import com.jonlatane.beatpad.instrument.MIDIInstrument
import com.jonlatane.beatpad.instrument.SequencerThread
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import com.jonlatane.beatpad.view.tempo.TempoTracking
import com.jonlatane.beatpad.view.topology.*
import kotlinx.android.synthetic.main.activity_main.*
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.startActivity
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : BaseActivity() {
    override val menuResource: Int = R.menu.main_menu
    private val playing = AtomicBoolean(false)
    private val executorService = Executors.newScheduledThreadPool(2)
    private lateinit var playingAdvice: Snackbar
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

        melody.instrument.instrument = GeneralMidiConstants.ELECTRIC_PIANO_0
        harmonicInstrument.instrument = GeneralMidiConstants.SYNTHBRASS_1
        sequencerInstrument.instrument = GeneralMidiConstants.STRING_ENSEMBLE_0

        val harmonyController = DeviceOrientationInstrument(harmonicInstrument)
        RhythmAnimations.wireMelodicControl(topology, harmonyController)
        keyboardIOHandler = KeyboardIOHandler(keyboard, melody.instrument)

        topology.onChordChangedListener = { c: Chord ->
            val tones = c.getTones(-60, 28)
            melody.tones = tones
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
        topology.intermediateMode()
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
        this.melody.instrument.instrument = savedInstanceState.getByte("melodicInstrument")
        val harmonicInstrument = savedInstanceState.getByte("harmonicInstrument")
        val sequencerInstrument = savedInstanceState.getByte("sequencerInstrument")
        this@MainActivity.harmonicInstrument.instrument = harmonicInstrument
        this@MainActivity.sequencerInstrument.instrument = sequencerInstrument
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("currentChord", topology.chord)
        outState.putInt("tempo", sequencerThread.beatsPerMinute)
        outState.putByte("melodicInstrument", melody.instrument.instrument)
        outState.putByte("harmonicInstrument", harmonicInstrument.instrument)
        outState.putByte("sequencerInstrument", sequencerInstrument.instrument)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.melody_instrument -> showInstrumentPicker(this, melody.instrument)
            R.id.harmony_instrument -> showInstrumentPicker(this, harmonicInstrument)
            R.id.sequencer_instrument -> showInstrumentPicker(this, sequencerInstrument)
            R.id.choose_tempo -> showTempoPicker(this)
            R.id.keyboard_toggle -> {
                if (keyboard.isHidden) {
                    keyboard.show()
                    item.title = "Use Color Keyboard"
                } else {
                    keyboard.hide()
                    item.title = "Use Piano Keyboard"
                }
                keyboard.toggleVisibility()
            }
            R.id.basic_mode -> topology.basicMode()
            R.id.intermediate_mode -> topology.intermediateMode()
            R.id.advanced_mode -> topology.advancedMode()
            R.id.chainsmokers_mode -> topology.chainsmokersMode()
            R.id.conduct -> startActivity<ConductorActivity>()
            R.id.play -> startActivity<InstrumentActivity>()
        }
        return true
    }

    override fun updateInstrumentNames() {
        menu.findItem(R.id.melody_instrument).title = "Melody: ${melody.instrument.instrumentName}"
        menu.findItem(R.id.harmony_instrument).title = "Harmony: ${harmonicInstrument.instrumentName}"
        menu.findItem(R.id.sequencer_instrument).title = "Sequencer: ${sequencerInstrument.instrumentName}"
    }

    internal fun updateTempoButton() {
        tempoTapper.text = "${sequencerThread.beatsPerMinute} BPM"
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
    }
}
