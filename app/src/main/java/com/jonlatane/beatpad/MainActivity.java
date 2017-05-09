package com.jonlatane.beatpad;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument;
import com.jonlatane.beatpad.instrument.SequencerThread;
import com.jonlatane.beatpad.instrument.MIDIInstrument;
import com.jonlatane.beatpad.sensors.Orientation;
import com.jonlatane.beatpad.view.topology.RhythmAnimations;
import com.jonlatane.beatpad.view.topology.TopologyView;

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
    private AtomicBoolean playing = new AtomicBoolean(false);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private Snackbar playingAdvice;
    SequencerThread sequencerThread;
    MIDIInstrument melodicInstrument;
    MIDIInstrument sequencerInstrument;
    TopologyView topology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topology = (TopologyView) findViewById(R.id.topology);

        //sequencerThread = new SequencerThread(new HarmonicOvertoneSeriesGenerator(), 120);
        sequencerInstrument = new MIDIInstrument();
        sequencerThread = new SequencerThread(sequencerInstrument, 120);
        melodicInstrument = new MIDIInstrument();
        melodicInstrument.channel = 1;

        playingAdvice = Snackbar.make(topology,
                "Tap the center chord to stop playback.", Snackbar.LENGTH_LONG);


        topology.addSequence(AUG_DIM);
        //topology.addSequence(CIRCLE_OF_FIFTHS);
        topology.addSequence(TWO_FIVE_ONE);
        topology.addSequence(REL_MINOR_MAJOR);
        //topology.addSequence(NINES);


        final DeviceOrientationInstrument melodyController = new DeviceOrientationInstrument(melodicInstrument);
        topology.setOnChordChangedListener(new TopologyView.OnChordChangedListener() {
            @Override
            public void onChordChanged(Chord c) {
                List<Integer> tones = c.getTones(-60, 60);
                melodyController.setTones(tones);
                sequencerThread.setTones(tones);
            }
        });
        topology.setChord(new Chord(0, MAJ_7));
        RhythmAnimations.wireMelodicControl(topology, melodyController);
        /*topology.setCurrentChordClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playing.getAndSet(true)) {
                    sequencerThread.stopped = false;
                    executorService.execute(sequencerThread);
                    playingAdvice.show();
                } else {
                    sequencerThread.stopped = true;
                    AudioTrackCache.releaseAll();
                    playing.set(false);
                    playingAdvice.dismiss();
                }
            }
        });*/
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
        final byte instrument = savedInstanceState.getByte("currentInstrument");
        MIDIInstrument.DRIVER.setOnMidiStartListener(new MidiDriver.OnMidiStartListener() {
            @Override
            public void onMidiStart() {
                MainActivity.this.sequencerInstrument.instrument = instrument;
                MainActivity.this.melodicInstrument.instrument = instrument;
            }
        });
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("currentChord", topology.getChord());
        outState.putByte("currentInstrument", sequencerInstrument.instrument);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_instrument:
                Dialogs.showInstrumentPicker(this);
                break;
            case R.id.select_tempo:
                Dialogs.showTempoPicker(this);
                break;
        }
        return true;
    }
}
