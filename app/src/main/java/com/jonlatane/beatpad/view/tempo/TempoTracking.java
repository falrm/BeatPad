package com.jonlatane.beatpad.view.tempo;

import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

/**
 * Created by jonlatane on 5/10/17.
 */

public class TempoTracking {
    public interface TempoChangeListener {
        void onTempoChanged(float tempo);
    }
    public static final void trackTempo(View v, final TempoChangeListener onTempoChanged) {
        trackTempo(v, 2, onTempoChanged);
    }
    public static final void trackTempo(
            View v,
            final int sampleWindowSize,
            final TempoChangeListener onTempoChanged
    ) {
        v.setOnTouchListener(new View.OnTouchListener() {
            long[] samples = new long[sampleWindowSize];
            int samplesTaken = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    samples[samplesTaken % sampleWindowSize] = System.currentTimeMillis();
                    if(++samplesTaken >= sampleWindowSize) {
                        reportTempo();
                        samplesTaken = (samplesTaken % sampleWindowSize) + sampleWindowSize;
                    }
                }
                return true;
            }

            private void reportTempo() {
                long[] sortedSamples = Arrays.copyOfRange(samples, 0, samples.length);
                Arrays.sort(sortedSamples);
                float[] partialBpms = new float[sortedSamples.length - 1];
                for(int i=0; i < partialBpms.length; i++) {
                    partialBpms[i] = 60000f/(float)(sortedSamples[i + 1] - sortedSamples[i]);
                }
                float avgBpm = 0;
                for (float partialBpm : partialBpms) {
                    avgBpm += partialBpm;
                }
                avgBpm /= partialBpms.length;

                onTempoChanged.onTempoChanged(avgBpm);
            }
        });
    }
}
