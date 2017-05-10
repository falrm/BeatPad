package com.jonlatane.beatpad;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument;
import com.jonlatane.beatpad.instrument.MIDIInstrument;
import com.jonlatane.beatpad.instrument.SequencerThread;
import com.jonlatane.beatpad.sensors.Orientation;
import com.jonlatane.beatpad.view.melody.MelodyView;
import com.jonlatane.beatpad.view.tempo.TempoTracking;
import com.jonlatane.beatpad.view.topology.RhythmAnimations;
import com.jonlatane.beatpad.view.topology.TopologyView;

import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jonlatane.beatpad.harmony.ChordAxis.AUG_DIM;
import static com.jonlatane.beatpad.harmony.ChordAxis.REL_MINOR_MAJOR;
import static com.jonlatane.beatpad.harmony.ChordAxis.TWO_FIVE_ONE;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ_7;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private AtomicBoolean playing = new AtomicBoolean(false);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private Snackbar playingAdvice;
    SequencerThread sequencerThread;
    MIDIInstrument melodicInstrument;
    MIDIInstrument harmonicInstrument;
    MIDIInstrument sequencerInstrument;
    MelodyView melody;
    TopologyView topology;
    Button seqButton;
    Button tempoButton;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        melody = (MelodyView) findViewById(R.id.melody);
        topology = (TopologyView) findViewById(R.id.topology);
        seqButton = (Button) findViewById(R.id.sequencerToggle);
        tempoButton = (Button) findViewById(R.id.tempoTapper);

        sequencerInstrument = new MIDIInstrument();
        sequencerThread = new SequencerThread(sequencerInstrument, 120);
        harmonicInstrument = new MIDIInstrument();
        melodicInstrument = new MIDIInstrument();

        harmonicInstrument.channel = 1;
        melodicInstrument.channel = 2;

        melodicInstrument.instrument = GeneralMidiConstants.ELECTRIC_PIANO_0;
        harmonicInstrument.instrument = GeneralMidiConstants.SYNTHBRASS_1;
        sequencerInstrument.instrument = GeneralMidiConstants.STRING_ENSEMBLE_0;

        topology.addSequence(AUG_DIM);
        //topology.addSequence(CIRCLE_OF_FIFTHS);
        topology.addSequence(TWO_FIVE_ONE);
        topology.addSequence(REL_MINOR_MAJOR);
        //topology.addSequence(NINES);


        final DeviceOrientationInstrument harmonyController = new DeviceOrientationInstrument(harmonicInstrument);
        topology.setOnChordChangedListener(new TopologyView.OnChordChangedListener() {
            @Override
            public void onChordChanged(Chord c) {
                List<Integer> tones = c.getTones(-60, 50);
                melody.setTones(tones);
                harmonyController.setTones(tones);
                sequencerThread.setTones(tones);
            }
        });
        topology.setChord(new Chord(0, MAJ_7));
        RhythmAnimations.wireMelodicControl(topology, harmonyController);
        melody.setInstrument(melodicInstrument);


        playingAdvice = Snackbar.make(topology,
                "Tap the SEQ button again to stop playback.", Snackbar.LENGTH_SHORT);
        seqButton.setOnClickListener(new View.OnClickListener() {
            boolean shown = false;
            @Override
            public void onClick(View v) {
                if(!playing.getAndSet(true)) {
                    sequencerThread.stopped = false;
                    executorService.execute(sequencerThread);
                    if(!shown) {
                        playingAdvice.show();
                        shown = true;
                    }
                } else {
                    sequencerThread.stopped = true;
                    AudioTrackCache.releaseAll();
                    playing.set(false);
                    playingAdvice.dismiss();
                }
            }
        });
        TempoTracking.trackTempo(tempoButton, new TempoTracking.TempoChangeListener() {
            @Override
            public void onTempoChanged(float tempo) {
                Log.i(TAG, "onTempoChanged:" + tempo);
                int bpm = Math.round(tempo);
                if(bpm > 20) {
                    sequencerThread.beatsPerMinute = bpm;
                    tempoButton.setText("TEMPO\n" + bpm);
                }
            }
        });
        Orientation.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MIDIInstrument.DRIVER.start();

        // Get the configuration.
        int[] config = MIDIInstrument.DRIVER.config();

        // Print out the details.
        Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
        Log.d(this.getClass().getName(), "numChannels: " + config[1]);
        Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
        Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
    }

    @Override
    public void onPause() {
        super.onPause();
        sequencerThread.stopped = true;
        AudioTrackCache.releaseAll();
        MIDIInstrument.DRIVER.stop();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Chord chord = savedInstanceState.getParcelable("currentChord");
        if(chord != null) {
            topology.setChord(chord);
        }
        final byte melodicInstrument = savedInstanceState.getByte("melodicInstrument");
        final byte harmonicInstrument = savedInstanceState.getByte("harmonicInstrument");
        final byte sequencerInstrument = savedInstanceState.getByte("sequencerInstrument");
        MainActivity.this.melodicInstrument.instrument = melodicInstrument;
        MainActivity.this.harmonicInstrument.instrument = harmonicInstrument;
        MainActivity.this.sequencerInstrument.instrument = sequencerInstrument;
        MIDIInstrument.DRIVER.setOnMidiStartListener(new MidiDriver.OnMidiStartListener() {
            @Override
            public void onMidiStart() {
                MainActivity.this.melodicInstrument.instrument = melodicInstrument;
                MainActivity.this.harmonicInstrument.instrument = harmonicInstrument;
                MainActivity.this.sequencerInstrument.instrument = sequencerInstrument;
                updateInstrumentNames();
            }
        });
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("currentChord", topology.getChord());
        outState.putByte("melodicInstrument", melodicInstrument.instrument);
        outState.putByte("harmonicInstrument", harmonicInstrument.instrument);
        outState.putByte("sequencerInstrument", sequencerInstrument.instrument);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        this.menu = menu;
        updateInstrumentNames();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.melody_instrument:
                Dialogs.showInstrumentPicker(this, melodicInstrument);
                break;
            case R.id.harmony_instrument:
                Dialogs.showInstrumentPicker(this, harmonicInstrument);
                break;
            case R.id.sequencer_instrument:
                Dialogs.showInstrumentPicker(this, sequencerInstrument);
                break;
            case R.id.select_tempo:
                Dialogs.showTempoPicker(this);
                break;
        }
        return true;
    }

    void updateInstrumentNames() {
        if(menu != null) {
            menu.findItem(R.id.melody_instrument).setTitle("Melody: " + melodicInstrument.getInstrumentName());
            menu.findItem(R.id.harmony_instrument).setTitle("Harmony: " + harmonicInstrument.getInstrumentName());
            menu.findItem(R.id.sequencer_instrument).setTitle("Sequencer: " + sequencerInstrument.getInstrumentName());
        }
    }
}
