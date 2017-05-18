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
    fun showInstrumentPicker(activity: MainActivity, instrument: MIDIInstrument) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose an instrument")
        builder.setItems(MIDIInstrument.MIDI_INSTRUMENT_NAMES.toTypedArray(), object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                instrument.instrument = which.toByte()
                activity.updateInstrumentNames()
            }
        })
        builder.show()
    }

    fun showTempoPicker(activity: MainActivity) {
        val dialog = Dialog(activity)
        dialog.setTitle("Select Tempo")
        dialog.setContentView(R.layout.dialog_choose_tempo)
        val picker = dialog.findViewById(R.id.numberPicker1) as NumberPicker
        picker.setMaxValue(960)
        picker.setMinValue(15)
        picker.setValue(activity.sequencerThread.beatsPerMinute)
        picker.setWrapSelectorWheel(false)
        picker.setOnValueChangedListener(object : NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
                val bpm = picker.getValue()
                activity.sequencerThread.beatsPerMinute = bpm
                activity.updateTempoButton()
            }
        })
        dialog.show()
    }

}
