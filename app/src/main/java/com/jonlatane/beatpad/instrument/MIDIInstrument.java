package com.jonlatane.beatpad.instrument;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by jonlatane on 5/8/17.
 */
public class MIDIInstrument implements Instrument {
    private static final String TAG = MIDIInstrument.class.getSimpleName();
    public static final MidiDriver DRIVER = new MidiDriver();
    public static final String[] MIDI_INSTRUMENT_NAMES;

    private final LinkedList<Integer> tones = new LinkedList<>();
    private final byte[] byte2 = new byte[2];
    private final byte[] byte3 = new byte[3];
    public byte channel = 0;
    public byte instrument = 0;

    @Override
    public void play(int tone) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
        play(tone, 64);
    }

    public void play(int tone, int velocity) {// Construct a note ON message for the middle C at maximum velocity on channel 1:
        selectInstrument(instrument);
        byte3[0] = (byte) (0x90 | channel);  // STATUS byte: note On, 0x00 = channel 1
        byte3[1] = (byte) (tone + 60);  // DATA byte: middle C = 60
        byte3[2] = (byte) velocity;  // DATA byte: maximum velocity = 127

        // Send the MIDI byte3 to the synthesizer.
        DRIVER.write(byte3);
        tones.add(tone);
    }

    @Override
    public void stop() {
        for(Integer tone : tones) {
            stop(tone);
        }
        tones.clear();
    }

    public void stop(int tone) {
        // Construct a note OFF message for the middle C at minimum velocity on channel 1:
        byte3[0] = (byte) (0x80 | channel);  // STATUS byte: 0x80 = note Off, 0x00 = channel 1
        byte3[1] = (byte) (tone + 60);  // 0x3C = middle C
        byte3[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)

        // Send the MIDI byte3 to the synthesizer.
        DRIVER.write(byte3);
    }

    public String getInstrumentName() {
        return MIDI_INSTRUMENT_NAMES[(int)instrument];
    }

    private MIDIInstrument selectInstrument(byte instrument) {
        this.instrument = instrument;
        byte2[0] = (byte) (0xC0 | channel);  // STATUS byte: Change, 0x00 = channel 1
        byte2[1] = instrument;
        DRIVER.write(byte2);
        return this;
    }

    static {
        MIDI_INSTRUMENT_NAMES = new String[128];
        Field[] declaredFields = GeneralMidiConstants.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                String name = WordUtils.capitalizeFully(field.getName().replace('_', ' '));
                int index = -1;
                try {
                    index = (int) (Byte) FieldUtils.readStaticField(field);
                } catch (IllegalAccessException e) {}
                MIDI_INSTRUMENT_NAMES[index] = name;
            }
        }
    }

    public static String[] getMidiInstruments() {
        return MIDI_INSTRUMENT_NAMES;
    }
}
