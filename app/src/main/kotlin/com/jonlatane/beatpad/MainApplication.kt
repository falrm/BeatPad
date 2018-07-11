package com.jonlatane.beatpad

import android.app.Application
import android.graphics.Typeface
import android.os.Build
import com.jonlatane.beatpad.midi.MidiDevices
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.sensors.ShakeDetector

class MainApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		instance = this
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			MidiDevices.initialize(this)
		}
		Orientation.initialize(this)
		ShakeDetector.initialize(this)
	}

	companion object {
		lateinit var instance: MainApplication
			private set

		val chordTypeface by lazy {
			Typeface.createFromAsset(instance.assets, "font/open_sans_regular.ttf")
		}
	}
}

