package com.jonlatane.beatpad.instrument;

import com.jonlatane.beatpad.sensors.Orientation;

import java.util.List;

/**
 * Created by jonlatane on 5/8/17.
 */

public class DeviceOrientationInstrument {
    public final Instrument instrument;
    public int toneSpread = 5;
    public int numSimultaneousTones = 5;
    private List<Integer> tones;

    public DeviceOrientationInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    public void setTones(List<Integer> tones) {
        this.tones = tones;
    }
    public void play() {
        // Play the notes
        if (tones != null) {
            // Normalize device's physical pitch to a number between 0 and 1
            float relativePitch = (-Orientation.pitch + 1.58f) / 3.14f;
            //Log.i(TAG, String.format("Relative pitch: %.2f", relativePitch));
            int toneIndex = Math.round((tones.size() - toneSpread) * relativePitch);
            for (int i = 0; i < numSimultaneousTones; i++) {
                instrument.play(tones.get(toneIndex + (i * toneSpread/numSimultaneousTones)));
            }
        }
    }
    public void stop() {
        instrument.stop();
    }
}
