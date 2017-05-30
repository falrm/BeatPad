package com.jonlatane.beatpad

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.MenuItem
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import kotlinx.android.synthetic.main.activity_instrument.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.contentView
import org.jetbrains.anko.info
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.concurrent.Executors


class InstrumentActivity : BaseActivity(), AnkoLogger {
    override val menuResource: Int = R.menu.instrument_menu
    lateinit var keyboardIOHandler: KeyboardIOHandler
    val keyboardInstrument = MIDIInstrument()
    var conductor: NsdServiceInfo? = null
    private val executorService = Executors.newScheduledThreadPool(2)
    @Volatile var requestedConductor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrument)
        keyboardInstrument.channel = 1
        keyboardIOHandler = KeyboardIOHandler(keyboard, keyboardInstrument)
        Orientation.initialize(this)
        startChordListener()
    }

    private fun startChordListener() {
        executorService.execute {
            while(true) {
                if(conductor != null) {
                    info("Connecting to $conductor")
                    try {
                        val socket = Socket(conductor!!.host, conductor!!.port)
                        val outputStream = socket.getOutputStream()
                        outputStream.flush()
                        val inputStream = socket.getInputStream()
                        val br = BufferedReader(InputStreamReader(inputStream))
                        val chordString = br.readText()
                        info("Received chord $chordString")
                        outputStream.close()
                        inputStream.close()
                        socket.close()
                        val root = chordString.substringBefore(':').trim().toInt()
                        val extension = chordString.substringAfter(':').split(',').map { it.trim().toInt() }
                        val chord = Chord(root, extension.toIntArray())
                        contentView?.post {
                            keyboardIOHandler.highlightChord(chord)
                            melody.tones = chord.getTones(-60, 28)
                        }
                    } catch(e: Throwable) {
                        Snackbar.make(contentView!!, "Failed to connect to conductor", Snackbar.LENGTH_SHORT).show()
                        info("Failed to connect to conductor $e")
                        conductor = null
                        contentView?.post {
                            keyboardIOHandler.highlightChord(null)
                            melody.tones = emptyList()
                        }
                    }
                } else if(!requestedConductor) {
                    //requestedConductor = true
                    //showConductorPicker(this) {
                    //    requestedConductor = false
                    //}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //showConductorPicker(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.color_instrument -> showInstrumentPicker(this, melody.instrument)
            R.id.keyboard_instrument -> showInstrumentPicker(this, keyboardInstrument)
            R.id.choose_conductor -> showConductorPicker(this)
        }
        return true
    }
    override fun updateInstrumentNames() {
        menu.findItem(R.id.color_instrument).title = "Color Instrument: ${melody.instrument.instrumentName}"
        menu.findItem(R.id.keyboard_instrument).title = "Keyboard Instrument: ${keyboardInstrument.instrumentName}"

    }
}