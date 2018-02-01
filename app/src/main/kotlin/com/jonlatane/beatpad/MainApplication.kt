package com.jonlatane.beatpad

import android.app.Application
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.sensors.ShakeDetector

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        MidiDevices.initialize(this)
        Orientation.initialize(this)
        ShakeDetector.initialize(this)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}

