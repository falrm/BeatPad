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

import static com.jonlatane.beatpad.harmony.Sequence.CIRCLE_OF_FIFTHS;
import static com.jonlatane.beatpad.harmony.Sequence.OCTAVES;
import static com.jonlatane.beatpad.harmony.Sequence.TWO_FIVE_ONE;

public class MainActivity extends AppCompatActivity {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    InstrumentThread instrumentThread;
    TopologyView topology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topology = (TopologyView) findViewById(R.id.topology);
        topology.addSequence(CIRCLE_OF_FIFTHS);
        topology.addSequence(TWO_FIVE_ONE);
        topology.addSequence(OCTAVES);
        instrumentThread = new InstrumentThread(new HarmonicOvertoneSeriesGenerator(), 480, 0.7f);
        topology.setOnChordChangedListener(new TopologyView.OnChordChangedListener() {
            @Override
            public void onChordChanged(Chord c) {
                instrumentThread.setTones(c.getTones());
            }
        });
        topology.setChord(new Chord(0, Chord.MAJOR_6, 2, 48));
        Orientation.initialize(this);
        executorService.execute(instrumentThread);
    }

    @Override
    public void onPause() {
        super.onPause();
        instrumentThread.stopped = true;
        AudioTrackCache.releaseAll();
    }
}
