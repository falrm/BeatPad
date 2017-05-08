package com.jonlatane.beatpad.midi;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.billthefarmer.mididriver.GeneralMidiConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonlatane on 5/8/17.
 */

public class MIDIUtilities {

    public interface InstrumentPickerHandler {
        void onSelect(byte choice);
    }
    public static void showInstrumentPicker(Context c, final InstrumentPickerHandler after) {
        CharSequence choices[] = getMidiInstruments();

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Choose an instrument");
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                after.onSelect((byte)which);
            }
        });
        builder.show();
    }

    private static String[] getMidiInstruments() {
        String[] result = new String[128];
        Field[] declaredFields = GeneralMidiConstants.class.getDeclaredFields();
        List<Field> staticFields = new ArrayList<>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                staticFields.add(field);
                String name = WordUtils.capitalizeFully(field.getName().replace('_', ' '));
                int index = -1;
                try {
                    index = (int) (Byte) FieldUtils.readStaticField(field);
                } catch (IllegalAccessException e) {}
                result[index] = name;
            }
        }
        return result;
    }
}
