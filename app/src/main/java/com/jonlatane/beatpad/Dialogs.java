package com.jonlatane.beatpad;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.NumberPicker;

import com.jonlatane.beatpad.instrument.MIDIInstrument;

/**
 * Utility class for things selected by dialog
 * Created by jonlatane on 5/8/17.
 */
class Dialogs {
    static void showInstrumentPicker(final MainActivity c, final MIDIInstrument instrument) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Choose an instrument");
        builder.setItems(MIDIInstrument.MIDI_INSTRUMENT_NAMES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                instrument.instrument = (byte) which;
                c.updateInstrumentNames();
            }
        });
        builder.show();
    }

    static void showTempoPicker(final MainActivity a) {
        final Dialog d = new Dialog(a);
        d.setTitle("Select Tempo");
        d.setContentView(R.layout.number_picker_dialog);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(960);
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

}
