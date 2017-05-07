package com.jonlatane.beatpad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.audio.generator.HarmonicOvertoneSeriesGenerator;
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.instrument.InstrumentThread;
import com.jonlatane.beatpad.sensors.Orientation;
import com.jonlatane.beatpad.view.TopologyView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jonlatane.beatpad.harmony.Sequence.*;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJOR_6;

public class MainActivity extends AppCompatActivity {
    private AtomicBoolean playing = new AtomicBoolean(false);
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    InstrumentThread instrumentThread;
    TopologyView topology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instrumentThread = new InstrumentThread(new HarmonicOvertoneSeriesGenerator(), 480, 0.9f);
        topology = (TopologyView) findViewById(R.id.topology);
        topology.addSequence(AUG_DIM);
        topology.addSequence(REL_MINOR_MAJOR);
        topology.addSequence(TWO_FIVE_ONE);
        topology.addSequence(CIRCLE_OF_FIFTHS);
        topology.addSequence(NINES);
        topology.setOnChordChangedListener(new TopologyView.OnChordChangedListener() {
            @Override
            public void onChordChanged(Chord c) {
                if(instrumentThread != null) instrumentThread.setTones(c.getTones(-36, 36));
            }
        });
        topology.setCurrentChordClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playing.getAndSet(true)) {
                    Toast.makeText(MainActivity.this, "playing", Toast.LENGTH_SHORT).show();
                    instrumentThread.stopped = false;
                    executorService.execute(instrumentThread);
                } else {
                    Toast.makeText(MainActivity.this, "stopping", Toast.LENGTH_SHORT).show();
                    instrumentThread.stopped = true;
                    AudioTrackCache.releaseAll();
                    playing.set(false);
                }
            }
        });
        Orientation.initialize(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(topology.getChord() == null) {
            topology.setChord(new Chord(0, MAJOR_6));
        }
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
        if(chord == null) {
            chord = new Chord(0, MAJOR_6);
        }
        topology.setChord(chord);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("currentChord", topology.getChord());
    }
}
