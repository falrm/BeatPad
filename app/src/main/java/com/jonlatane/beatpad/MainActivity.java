package com.jonlatane.beatpad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jonlatane.beatpad.audio.AudioTrackCache;
import com.jonlatane.beatpad.audio.generator.HarmonicOvertoneSeriesGenerator;
import com.jonlatane.beatpad.harmony.Chord;
import com.jonlatane.beatpad.instrument.InstrumentThread;
import com.jonlatane.beatpad.sensors.Orientation;
import com.jonlatane.beatpad.view.TopologyView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.jonlatane.beatpad.harmony.Sequence.*;

public class MainActivity extends AppCompatActivity {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    InstrumentThread instrumentThread;
    TopologyView topology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topology = (TopologyView) findViewById(R.id.topology);
        topology.addSequence(OCTAVES);
        topology.addSequence(TWO_FIVE_ONE);
        topology.addSequence(CIRCLE_OF_FIFTHS);
        topology.addSequence(AUG_DIM);
        topology.addSequence(CHROMATIC);
        topology.setOnChordChangedListener(new TopologyView.OnChordChangedListener() {
            @Override
            public void onChordChanged(Chord c) {
                if(instrumentThread != null) instrumentThread.setTones(c.getTones());
            }
        });
        Orientation.initialize(this);
    }

    public void onResume() {
        super.onResume();
        instrumentThread = new InstrumentThread(new HarmonicOvertoneSeriesGenerator(), 480, 0.9f);
        executorService.execute(instrumentThread);
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
            chord = new Chord(0, Chord.MAJOR_6, 2, 48);
        }
        topology.setChord(chord);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("currentChord", topology.getChord());
    }
}
