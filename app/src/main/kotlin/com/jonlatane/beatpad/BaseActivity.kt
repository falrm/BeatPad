package com.jonlatane.beatpad

import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

abstract class BaseActivity : AppCompatActivity(), AnkoLogger {
    lateinit var menu: Menu
    abstract val menuResource: Int

    abstract fun updateInstrumentNames()
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(menuResource, menu)
        this.menu = menu
        updateInstrumentNames()
        return true
    }

    override fun onResume() {
        super.onResume()
        MIDIInstrument.DRIVER.start()

        // Get the configuration.
        val config = MIDIInstrument.DRIVER.config()

        // Print out the details.
        debug("maxVoices: " + config[0])
        debug("numChannels: " + config[1])
        debug("sampleRate: " + config[2])
        debug("mixBufferSize: " + config[3])
    }

    override fun onPause() {
        super.onPause()
        AudioTrackCache.releaseAll()
        MIDIInstrument.DRIVER.stop()
    }
}