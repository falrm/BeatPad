package com.jonlatane.beatpad

import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.jonlatane.beatpad.midi.AndroidMidi
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

abstract class BaseActivity : AppCompatActivity(), AnkoLogger {
    lateinit var menu: Menu
    abstract val menuResource: Int

    abstract fun updateMenuOptions()
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(menuResource, menu)
        this.menu = menu
        updateMenuOptions()
        return true
    }
}