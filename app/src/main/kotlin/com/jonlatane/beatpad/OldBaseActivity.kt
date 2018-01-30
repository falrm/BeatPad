package com.jonlatane.beatpad

import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

abstract class OldBaseActivity : AppCompatActivity(), AnkoLogger {
    lateinit var menu: Menu
    abstract val menuResource: Int

    abstract fun updateMenuOptions()
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(menuResource, menu)
        this.menu = menu
        updateMenuOptions()
        return true
    }

    override fun onResume() {
        super.onResume()
        AndroidMidi.ONBOARD_DRIVER.start()

        // Get the configuration.
        val config = AndroidMidi.ONBOARD_DRIVER.config()

        // Print out the details.
        debug("maxVoices: " + config[0])
        debug("numChannels: " + config[1])
        debug("sampleRate: " + config[2])
        debug("mixBufferSize: " + config[3])
    }

    override fun onPause() {
        super.onPause()
        AudioTrackCache.releaseAll()
        AndroidMidi.ONBOARD_DRIVER.stop()
    }
}