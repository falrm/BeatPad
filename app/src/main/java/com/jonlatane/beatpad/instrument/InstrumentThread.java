package com.jonlatane.beatpad.instrument;

import com.jonlatane.beatpad.sensors.Orientation;

import java.util.List;

/**
 * Created by jonlatane on 5/5/17.
 */
public class InstrumentThread implements Runnable {
    private static final String TAG = InstrumentThread.class.getSimpleName();

    public volatile int beatsPerMinute;
    public volatile boolean stopped = false;

    private boolean[] subDivisions = {true};

    private final Instrument instrument;
    private List<Integer> tones;

    private int toneSpread = 5;
    private int numSimultaneousTones = 5;

    /**
     * Creates a playback thread for the given instrument.
     *
     * @param instrument
     * @param beatsPerMinute
     */
    public InstrumentThread(Instrument instrument, Integer beatsPerMinute, boolean... subDivisons) {
        this.instrument = instrument;
        this.beatsPerMinute = beatsPerMinute;
    }

    public void setTones(List<Integer> tones) {
        this.tones = tones;
    }

    @Override
    public void run() {
        while(!stopped) {
            playBeat();
        }
    }

    private void playBeat() {
        try {
            long msBetweenSubdivisions = 60000L / (beatsPerMinute * subDivisions.length);

            for (boolean subDivision : subDivisions) {
                // Roll as a number between -0.5 and 0.5
                float relativeRoll = Orientation.roll;
                if(relativeRoll > 1.62) relativeRoll -= 1.62;
                if(relativeRoll <-1.62) relativeRoll += 1.62;
                relativeRoll /= 3.14;
                // Normalize it to the range [0.3f, 1.0f]
                relativeRoll = Math.min(Math.max(0.3f, (3f * relativeRoll * 0.7f) + 0.65f), 1.0f);
                long playDuration = (long) (relativeRoll * msBetweenSubdivisions);
                long pauseDuration = msBetweenSubdivisions - playDuration;

                // Interpret the booleans as "play" or "rest"
                if(subDivision) {
                    playSubdivision(playDuration, pauseDuration);
                } else {
                    Thread.sleep(msBetweenSubdivisions);
                }
                if(stopped) {
                    break;
                }
            }
        } catch (InterruptedException ignored) { }
    }

    private void playSubdivision(long playDuration, long pauseDuration) throws InterruptedException {
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
        Thread.sleep(playDuration);
        instrument.stop();
        Thread.sleep(pauseDuration);
    }
}
