package com.jonlatane.beatpad.instrument;

import com.jonlatane.beatpad.sensors.Orientation;

/**
 * Created by jonlatane on 5/5/17.
 */
public class SequencerThread extends DeviceOrientationInstrument implements Runnable {
    private static final String TAG = SequencerThread.class.getSimpleName();

    public volatile int beatsPerMinute;
    public volatile boolean stopped = false;

    private boolean[] subDivisions = {true, true};



    /**
     * Creates a playback thread for the given instrument.
     *
     * @param instrument
     * @param beatsPerMinute
     */
    public SequencerThread(Instrument instrument, Integer beatsPerMinute) {
        super(instrument);
        this.beatsPerMinute = beatsPerMinute;
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
                float relativeRoll = getRollForArticulation();
                long playDuration = (long) (relativeRoll * msBetweenSubdivisions);
                long pauseDuration = msBetweenSubdivisions - playDuration;

                // Interpret the booleans as "play" or "rest"
                if(subDivision) {
                    play();
                    Thread.sleep(playDuration);
                    stop();
                    Thread.sleep(pauseDuration);
                } else {
                    Thread.sleep(msBetweenSubdivisions);
                }
                if(stopped) {
                    break;
                }
            }
        } catch (InterruptedException ignored) { }
    }

    /**
     * Return device roll in the range
     * @return
     */
    private float getRollForArticulation() {
        // Roll is in the range [-0.5. 0.5] Normalize it to the range [0.3f, 1.0f]
        float result = Math.min(Math.max(0.3f, (3f * Orientation.normalizedDeviceRoll() * 0.7f) + 0.65f), 1.0f);
        return result;
    }
}
