package com.jonlatane.beatpad

import android.app.Application
import android.graphics.drawable.GradientDrawable
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.sensors.Orientation

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        MidiDevices.initialize(this)
        Orientation.initialize(this)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}

