package com.jonlatane.beatpad;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.NumberPicker;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.billthefarmer.mididriver.GeneralMidiConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for things selected by dialog
 * Created by jonlatane on 5/8/17.
 */
class Dialogs {
    static void showInstrumentPicker(final MainActivity c) {
        CharSequence choices[] = getMidiInstruments();

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Choose an instrument");
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                c.sequencerInstrument.instrument = (byte) which;
                c.melodicInstrument.instrument = (byte) which;
            }
        });
        builder.show();
    }

    static void showTempoPicker(final MainActivity a) {
        final Dialog d = new Dialog(a);
        d.setTitle("Select Tempo");
        d.setContentView(R.layout.number_picker_dialog);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(480);
        np.setMinValue(15);
        np.setValue(a.sequencerThread.beatsPerMinute);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                a.sequencerThread.beatsPerMinute = np.getValue();
            }
        });
        d.show();
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
