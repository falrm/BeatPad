package com.jonlatane.beatpad

import android.app.Application
import com.jonlatane.beatpad.midi.MidiDevices

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        MidiDevices.initialize(this)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}

