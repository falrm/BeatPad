package com.jonlatane.beatpad

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button

import com.jonlatane.beatpad.audio.AudioTrackCache
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument
import com.jonlatane.beatpad.instrument.MIDIInstrument
import com.jonlatane.beatpad.instrument.SequencerThread
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import com.jonlatane.beatpad.view.keyboard.KeyboardView
import com.jonlatane.beatpad.view.melody.MelodyView
import com.jonlatane.beatpad.view.tempo.TempoTracking
import com.jonlatane.beatpad.view.topology.RhythmAnimations
import com.jonlatane.beatpad.view.topology.TopologyView

import org.billthefarmer.mididriver.GeneralMidiConstants
import org.billthefarmer.mididriver.MidiDriver
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import com.jonlatane.beatpad.harmony.*
import com.jonlatane.beatpad.harmony.chord.*

class MainActivity : AppCompatActivity() {
    private val playing = AtomicBoolean(false)
    private val executorService = Executors.newScheduledThreadPool(2)
    private lateinit var playingAdvice: Snackbar
    internal lateinit var sequencerThread: SequencerThread
    internal lateinit var melodicInstrument: MIDIInstrument
    internal lateinit var harmonicInstrument: MIDIInstrument
    internal lateinit var sequencerInstrument: MIDIInstrument
    internal lateinit var keyboardIOHandler: KeyboardIOHandler
    internal lateinit var keyboard: KeyboardView
    internal lateinit var melody: MelodyView
    internal lateinit var topology: TopologyView
    internal lateinit var seqButton: Button
    internal lateinit var tempoButton: Button
    internal lateinit var menu: Menu

    override protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        keyboard = findViewById(R.id.keyboard) as KeyboardView
        melody = findViewById(R.id.melody) as MelodyView
        topology = findViewById(R.id.topology) as TopologyView
        seqButton = findViewById(R.id.sequencerToggle) as Button
        tempoButton = findViewById(R.id.tempoTapper) as Button

        sequencerInstrument = MIDIInstrument()
        sequencerThread = SequencerThread(sequencerInstrument, 120)
        harmonicInstrument = MIDIInstrument()
        melodicInstrument = MIDIInstrument()

        harmonicInstrument.channel = 1
        melodicInstrument.channel = 2

        melodicInstrument.instrument = GeneralMidiConstants.ELECTRIC_PIANO_0
        harmonicInstrument.instrument = GeneralMidiConstants.SYNTHBRASS_1
        sequencerInstrument.instrument = GeneralMidiConstants.STRING_ENSEMBLE_0

        melody.setInstrument(melodicInstrument)
        val harmonyController = DeviceOrientationInstrument(harmonicInstrument)
        RhythmAnimations.wireMelodicControl(topology, harmonyController)
        keyboardIOHandler = KeyboardIOHandler(keyboard, melodicInstrument)

        topology.onChordChangedListener = { c: Chord ->
            val tones = c.getTones(-60, 50)
            melody.setTones(tones)
            harmonyController.setTones(tones)
            sequencerThread.setTones(tones)
            keyboardIOHandler.highlightChord(c)
        }

        playingAdvice = Snackbar.make(topology,
                "Tap the SEQ button again to stop playback.", Snackbar.LENGTH_SHORT)
        seqButton.setOnClickListener(object : View.OnClickListener {
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
        TempoTracking.trackTempo(tempoButton, object : TempoTracking.TempoChangeListener {
            override fun onTempoChanged(tempo: Float) {
                Log.i(TAG, "onTempoChanged:" + tempo)
                val bpm = Math.round(tempo)
                if (bpm > 20) {
                    sequencerThread.beatsPerMinute = bpm
                    updateTempoButton()
                }
            }
        })
        updateTempoButton()
        Orientation.initialize(this)
        intermediateMode()
    }

    override protected fun onResume() {
        super.onResume()
        MIDIInstrument.DRIVER.start()

        // Get the configuration.
        val config = MIDIInstrument.DRIVER.config()

        // Print out the details.
        Log.d(TAG, "maxVoices: " + config[0])
        Log.d(TAG, "numChannels: " + config[1])
        Log.d(TAG, "sampleRate: " + config[2])
        Log.d(TAG, "mixBufferSize: " + config[3])
        topology.onResume()
    }

    override fun onPause() {
        super.onPause()
        sequencerThread.stopped = true
        AudioTrackCache.releaseAll()
        MIDIInstrument.DRIVER.stop()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val chord = savedInstanceState.getParcelable<Chord>("currentChord")
        if (chord != null) {
            topology.chord = chord
        }
        sequencerThread.beatsPerMinute = savedInstanceState.getInt("tempo")
        updateTempoButton()
        val melodicInstrument = savedInstanceState.getByte("melodicInstrument")
        val harmonicInstrument = savedInstanceState.getByte("harmonicInstrument")
        val sequencerInstrument = savedInstanceState.getByte("sequencerInstrument")
        this@MainActivity.melodicInstrument.instrument = melodicInstrument
        this@MainActivity.harmonicInstrument.instrument = harmonicInstrument
        this@MainActivity.sequencerInstrument.instrument = sequencerInstrument
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("currentChord", topology.chord)
        outState.putInt("tempo", sequencerThread.beatsPerMinute)
        outState.putByte("melodicInstrument", melodicInstrument.instrument)
        outState.putByte("harmonicInstrument", harmonicInstrument.instrument)
        outState.putByte("sequencerInstrument", sequencerInstrument.instrument)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        updateInstrumentNames()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.melody_instrument -> Dialogs.showInstrumentPicker(this, melodicInstrument)
            R.id.harmony_instrument -> Dialogs.showInstrumentPicker(this, harmonicInstrument)
            R.id.sequencer_instrument -> Dialogs.showInstrumentPicker(this, sequencerInstrument)
            R.id.choose_tempo -> Dialogs.showTempoPicker(this)
            R.id.keyboard_toggle -> {
                if (keyboard.isHidden) {
                    keyboard.show()
                    item.setTitle("Use Color Keyboard")
                } else {
                    keyboard.hide()
                    item.setTitle("Use Piano Keyboard")
                }
                keyboard.toggleVisibility()
            }
            R.id.basic_mode -> basicMode()
            R.id.intermediate_mode -> intermediateMode()
            R.id.advanced_mode -> advancedMode()
            R.id.chainsmokers_mode -> chainsmokersMode()
        }
        return true
    }

    internal fun updateInstrumentNames() {
        menu.findItem(R.id.melody_instrument).setTitle("Melody: ${melodicInstrument.instrumentName}")
        menu.findItem(R.id.harmony_instrument).setTitle("Harmony: ${harmonicInstrument.instrumentName}")
        menu.findItem(R.id.sequencer_instrument).setTitle("Sequencer: ${sequencerInstrument.instrumentName}")
    }

    internal fun updateTempoButton() {
        tempoButton.setText("${sequencerThread.beatsPerMinute} BPM")
    }

    internal fun basicMode() {
        topology.removeSequence(CHAINSMOKERS)
        topology.removeSequence(AUG_DIM)
        topology.removeSequence(CIRCLE_OF_FIFTHS)
        topology.removeSequence(WHOLE_STEPS)
        topology.removeSequence(REL_MINOR_MAJOR)
        topology.addSequence(0, TWO_FIVE_ONE)
    }

    internal fun intermediateMode() {
        topology.removeSequence(CHAINSMOKERS)
        topology.removeSequence(CIRCLE_OF_FIFTHS)
        topology.removeSequence(WHOLE_STEPS)
        topology.addSequence(0, AUG_DIM)
        topology.addSequence(1, TWO_FIVE_ONE)
        topology.addSequence(2, REL_MINOR_MAJOR)
    }

    internal fun advancedMode() {
        topology.removeSequence(CHAINSMOKERS)
        topology.addSequence(0, AUG_DIM)
        topology.addSequence(1, CIRCLE_OF_FIFTHS)
        topology.addSequence(2, TWO_FIVE_ONE)
        topology.addSequence(3, WHOLE_STEPS)
        topology.addSequence(4, REL_MINOR_MAJOR)
    }

    internal fun chainsmokersMode() {
        topology.removeSequence(AUG_DIM)
        topology.removeSequence(WHOLE_STEPS)
        topology.removeSequence(TWO_FIVE_ONE)
        topology.addSequence(0, CIRCLE_OF_FIFTHS)
        topology.addSequence(1, CHAINSMOKERS)
        topology.addSequence(2, REL_MINOR_MAJOR)
    }

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()
    }
}
