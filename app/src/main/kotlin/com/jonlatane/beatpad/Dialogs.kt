package com.jonlatane.beatpad

import android.app.Dialog
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.widget.NumberPicker

import com.jonlatane.beatpad.instrument.MIDIInstrument

/**
 * Utility class for things selected by dialog
 * Created by jonlatane on 5/8/17.
 */
internal object Dialogs {
    fun showInstrumentPicker(c: MainActivity, instrument: MIDIInstrument) {
        val builder = AlertDialog.Builder(c)
        builder.setTitle("Choose an instrument")
        builder.setItems(MIDIInstrument.MIDI_INSTRUMENT_NAMES.toTypedArray(), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                instrument.instrument = which.toByte()
                c.updateInstrumentNames()
            }
        })
        builder.show()
    }

    fun showTempoPicker(a: MainActivity) {
        val dialog = Dialog(a)
        dialog.setTitle("Select Tempo")
        dialog.setContentView(R.layout.dialog_choose_tempo)
        val np = dialog.findViewById(R.id.numberPicker1) as NumberPicker
        np.setMaxValue(960)
        np.setMinValue(15)
        np.setValue(a.sequencerThread.beatsPerMinute)
        np.setWrapSelectorWheel(false)
        np.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
                val bpm = np.getValue()
                a.sequencerThread.beatsPerMinute = bpm
                a.updateTempoButton()
            }
        })
        dialog.show()
    }

}
