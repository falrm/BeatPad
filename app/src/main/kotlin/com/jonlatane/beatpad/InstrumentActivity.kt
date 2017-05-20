package com.jonlatane.beatpad

import android.os.Bundle
import android.view.MenuItem
import com.jonlatane.beatpad.instrument.MIDIInstrument
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import kotlinx.android.synthetic.main.activity_instrument.*

class InstrumentActivity : BaseActivity() {
    override val menuResource: Int = R.menu.instrument_menu
    lateinit var keyboardIOHandler: KeyboardIOHandler
    val keyboardInstrument = MIDIInstrument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrument)
        keyboardInstrument.channel = 1
        keyboardIOHandler = KeyboardIOHandler(keyboard, keyboardInstrument)
        Orientation.initialize(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.color_instrument -> showInstrumentPicker(this, melody.instrument)
            R.id.keyboard_instrument -> showInstrumentPicker(this, keyboardInstrument)
        }
        return true
    }
    override fun updateInstrumentNames() {
        menu.findItem(R.id.color_instrument).title = "Color Instrument: ${melody.instrument.instrumentName}"
        menu.findItem(R.id.keyboard_instrument).title = "Keyboard Instrument: ${keyboardInstrument.instrumentName}"

    }
}