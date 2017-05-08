package com.jonlatane.beatpad;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.audio.generator.HarmonicOvertoneSeriesGenerator;
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.instrument.InstrumentThread;
import com.jonlatane.beatpad.sensors.Orientation;
import com.jonlatane.beatpad.view.TopologyView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jonlatane.beatpad.harmony.Sequence.AUG_DIM;
import static com.jonlatane.beatpad.harmony.Sequence.REL_MINOR_MAJOR;
import static com.jonlatane.beatpad.harmony.Sequence.TWO_FIVE_ONE;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ_7;

public class MainActivity extends AppCompatActivity {
    private AtomicBoolean playing = new AtomicBoolean(false);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private Snackbar playingAdvice;
    InstrumentThread instrumentThread;
    TopologyView topology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instrumentThread = new InstrumentThread(new HarmonicOvertoneSeriesGenerator(), 120);
        topology = (TopologyView) findViewById(R.id.topology);
        playingAdvice = Snackbar.make(topology,
                "Tap the center chord to stop playback.", Snackbar.LENGTH_LONG);
        topology.addSequence(AUG_DIM);
        //topology.addSequence(CIRCLE_OF_FIFTHS);
        topology.addSequence(TWO_FIVE_ONE);
        topology.addSequence(REL_MINOR_MAJOR);
        //topology.addSequence(NINES);
        topology.setOnChordChangedListener(new TopologyView.OnChordChangedListener() {
            @Override
            public void onChordChanged(Chord c) {
                instrumentThread.setTones(c.getTones(-36, 36));
            }
        });
        topology.setChord(new Chord(0, MAJ_7));
        topology.setCurrentChordClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playing.getAndSet(true)) {
                    instrumentThread.stopped = false;
                    executorService.execute(instrumentThread);
                    playingAdvice.show();
                } else {
                    instrumentThread.stopped = true;
                    AudioTrackCache.releaseAll();
                    playing.set(false);
                    playingAdvice.dismiss();
                }
            }
        });
        Orientation.initialize(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        instrumentThread.stopped = true;
        AudioTrackCache.releaseAll();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Chord chord = savedInstanceState.getParcelable("currentChord");
        if(chord != null) {
            topology.setChord(chord);
        }
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("currentChord", topology.getChord());
    }
}
