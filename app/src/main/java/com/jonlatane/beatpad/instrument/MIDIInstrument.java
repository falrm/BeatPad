package com.jonlatane.beatpad.instrument;

import org.billthefarmer.mididriver.MidiDriver;

import java.util.LinkedList;

/**
 * Created by jonlatane on 5/8/17.
 */

public class MIDIInstrument implements Instrument {
    public static final MidiDriver DRIVER = new MidiDriver();
    public static final byte PIANO = (byte)60;
    private static final String TAG = MIDIInstrument.class.getSimpleName();

    private final LinkedList<Integer> tones = new LinkedList<>();
    private final byte[] event = new byte[3];
    public byte channel = 0;
    public byte instrument = 0;

    @Override
    public void play(int tone) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
        selectInstrument(instrument);
        event[0] = (byte) (0x90 | channel);  // STATUS byte: note On, 0x00 = channel 1
        event[1] = (byte) (tone + 60);  // DATA byte: middle C = 60
        event[2] = (byte) 64;  // DATA byte: maximum velocity = 127

        // Internally this just calls write() and can be considered obsoleted:
        //midiDriver.queueEvent(event);

        // Send the MIDI event to the synthesizer.
        DRIVER.write(event);
        tones.add(tone);
    }

    @Override
    public void stop() {
        for(Integer tone : tones) {
            // Construct a note OFF message for the middle C at minimum velocity on channel 1:
            event[0] = (byte) (0x80 | channel);  // STATUS byte: 0x80 = note Off, 0x00 = channel 1
            event[1] = (byte) (tone + 60);  // 0x3C = middle C
            event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)

            // Send the MIDI event to the synthesizer.
            DRIVER.write(event);
        }
        tones.clear();
    }

    private MIDIInstrument selectInstrument(byte instrument) {
        this.instrument = instrument;
        byte[] changeEvent = new byte[2];
        changeEvent[0] = (byte) (0xC0 | channel);  // STATUS byte: Change, 0x00 = channel 1
        changeEvent[1] = instrument;
        DRIVER.write(changeEvent);
        return this;
    }
}
