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
    private long playDuration;
    private long pauseDuration;
    private long syncDelay = 0;
    private final Instrument instrument;
    private AudioTrack[] tracks = new AudioTrack[3];
    private List<Integer> tones;
    public volatile boolean stopped = false;

    /**
     * Two InstrumentThreads should not share the same AudioTrackGenerator.
     *
     * @param generator
     * @param beatsPerMinute
     */
    public InstrumentThread(AudioTrackGenerator generator, Integer beatsPerMinute, Float holdAmount) {
        if(holdAmount <= 0 || holdAmount >= 1) {
            throw new IllegalArgumentException("nope");
        }
        this.instrument = new Instrument(generator);
        Long msBetweenBeats = 60000L / beatsPerMinute.longValue();
        playDuration = (long) (holdAmount * msBetweenBeats);
        pauseDuration = msBetweenBeats - playDuration;
    }

    public void setTones(List<Integer> tones) {
        this.tones = tones;
    }

    @Override
    public void run() {
        boolean playing = false;
        while(!stopped) {
            long delay;
            if(playing) {
                for(AudioTrack track : tracks) {
                    instrument.stop(track);
                }
                playing = false;
                delay = pauseDuration;
            } else {
                //Log.i(TAG, String.format("Azimuth: %.2f, Pitch: %.2f, Roll: %.2f, Inclination: %.2f",
                //        Orientation.azimuth, Orientation.pitch, Orientation.roll, Orientation.inclination));
                if(tones != null) {
                    float relativePitch = (-Orientation.pitch + 1.58f) / 3.14f;
                    //Log.i(TAG, String.format("Relative pitch: %.2f", relativePitch));
                    int toneIndex = Math.round((tones.size() - tracks.length) * relativePitch);
                    for(int i = 0; i < tracks.length; i++) {
                        tracks[i] = instrument.play(tones.get(toneIndex + i));
                    }
                    playing = true;
                }
                delay = playDuration;
            }
            try {
                Thread.sleep(delay + syncDelay);
                syncDelay = 0;
            } catch (InterruptedException ignored) { }
        }
    }
}
