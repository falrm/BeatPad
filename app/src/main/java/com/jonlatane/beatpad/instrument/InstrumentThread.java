package com.jonlatane.beatpad.instrument;

import android.media.AudioTrack;

import com.jonlatane.beatpad.audio.AudioTrackGenerator;
import com.jonlatane.beatpad.sensors.Orientation;

import java.util.List;

/**
 * Created by jonlatane on 5/5/17.
 */
public class InstrumentThread implements Runnable {
    private static final String TAG = InstrumentThread.class.getSimpleName();

    private int beatsPerMinute;
    private boolean[] subDivisions = {true, false, true};

    private final Instrument instrument;
    private List<Integer> tones;
    private int harmonicThickness = 4;
    public volatile boolean stopped = false;

    /**
     * Two InstrumentThreads should not share the same AudioTrackGenerator.
     *
     * @param generator
     * @param beatsPerMinute
     */
    public InstrumentThread(AudioTrackGenerator generator, Integer beatsPerMinute, boolean... subDivisons) {
        this.instrument = new Instrument(generator);
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
        AudioTrack[] tracks = new AudioTrack[harmonicThickness];

        try {
            long msBetweenSubdivisions = 60000L / (beatsPerMinute * subDivisions.length);

            for (boolean subDivision : subDivisions) {
                // Roll as a number between 0 and 1
                float relativeRoll = 1 - (-Orientation.roll + 1.58f) / 3.14f;
                // Normalize it to the range [0.2, 0.8]
                relativeRoll = Math.min(Math.max(0.05f, relativeRoll * 2 - 0.4f), 0.95f);
                long playDuration = (long) (relativeRoll * msBetweenSubdivisions);
                long pauseDuration = msBetweenSubdivisions - playDuration;

                // Interpret the booleans as "play" or "rest"
                if(subDivision) {
                    playSubdivision(tracks, playDuration, pauseDuration);
                } else {
                    Thread.sleep(msBetweenSubdivisions);
                }
            }
        } catch (InterruptedException ignored) { }
    }

    private void playSubdivision(AudioTrack[] tracks, long playDuration, long pauseDuration) throws InterruptedException {
        // Play the notes
        if (tones != null) {
            // Normalize device's physical pitch to a number between 0 and 1
            float relativePitch = (-Orientation.pitch + 1.58f) / 3.14f;
            //Log.i(TAG, String.format("Relative pitch: %.2f", relativePitch));
            int toneIndex = Math.round((tones.size() - tracks.length) * relativePitch);
            for (int i = 0; i < tracks.length; i++) {
                tracks[i] = instrument.play(tones.get(toneIndex + i));
            }
        }
        Thread.sleep(playDuration);
        for(AudioTrack track : tracks) {
            instrument.stop(track);
        }
        Thread.sleep(pauseDuration);
    }
}
