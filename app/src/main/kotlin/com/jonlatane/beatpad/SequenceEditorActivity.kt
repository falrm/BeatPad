package com.jonlatane.beatpad

import android.app.Activity
import android.os.Bundle
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.view.tonesequence.ToneSequenceUI
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.setContentView

class SequenceEditorActivity : Activity(), AnkoLogger {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		info("hi hi hi hi")
		ToneSequenceUI().setContentView(this)
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