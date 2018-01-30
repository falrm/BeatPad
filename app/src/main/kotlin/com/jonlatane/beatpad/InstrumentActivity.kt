package com.jonlatane.beatpad

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.MenuItem
import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.view.keyboard.KeyboardIOHandler
import kotlinx.android.synthetic.main.activity_instrument.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.contentView
import org.jetbrains.anko.info
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.util.concurrent.Executors


class InstrumentActivity : OldBaseActivity(), AnkoLogger {
    override val menuResource: Int = R.menu.instrument_menu
    lateinit var keyboardIOHandler: KeyboardIOHandler
    lateinit var keyboardInstrument: Instrument
    var conductor: NsdServiceInfo? = null
    private val executorService = Executors.newScheduledThreadPool(2)
    @Volatile var requestedConductor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instrument)
        keyboardIOHandler = KeyboardIOHandler(keyboard)
        (keyboardIOHandler.instrument as? MIDIInstrument)?.apply {
            channel = 1
        }
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
                            colorboard.chord = chord
                        }
                    } catch(e: Throwable) {
                        Snackbar.make(contentView!!, "Failed to connect to conductor", Snackbar.LENGTH_SHORT).show()
                        info("Failed to connect to conductor $e")
                        conductor = null
                        contentView?.post {
                            keyboardIOHandler.highlightChord(null)
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
            R.id.color_instrument -> (colorboard.instrument as? MIDIInstrument)?.let {
                showInstrumentPicker(it, this)
            }
            R.id.keyboard_instrument -> (keyboardInstrument as? MIDIInstrument)?.let {
                showInstrumentPicker(it, this)
            }
            R.id.choose_conductor -> showConductorPicker(this)
        }
        return true
    }
    override fun updateMenuOptions() {
        menu.findItem(R.id.color_instrument).title = "Color Instrument: ${colorboard.instrument.instrumentName}"
        menu.findItem(R.id.keyboard_instrument).title = "Keyboard Instrument: ${keyboardInstrument.instrumentName}"

    }
}