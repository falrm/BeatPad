package com.jonlatane.beatpad

import BeatClockPaletteConsumer
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.audiotrack.AudioTrackCache
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.sensors.ShakeDetector
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.formatted
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.palette.PaletteUI
import org.billthefarmer.mididriver.GeneralMidiConstants
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast


class PaletteEditorActivity : Activity(), AnkoLogger {
	private lateinit var ui: PaletteUI
	private val viewModel get() = ui.viewModel
	private var lastBackPress: Long? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		ui = PaletteUI().also {
			it.setContentView(this)
		}

		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		viewModel.palette = Storage.loadPalette(this)

		val bundle = savedInstanceState ?: try {
			intent.extras.getBundle("playgroundState")
		} catch (t: Throwable) {
			savedInstanceState
		}

		if (bundle != null) {
			println("Got intent with extras: ${bundle.formatted()}")
			onRestoreInstanceState(bundle)
		}
	}

	override fun onBackPressed() {
		when {
			viewModel.onBackPressed() -> {}
			lastBackPress?.let { System.currentTimeMillis() - it <  3000 } ?: false -> {
				val intent = Intent(this, PlaybackService::class.java)
				intent.action = PlaybackService.Companion.Action.STOPFOREGROUND_ACTION
				startService(intent)
				super.onBackPressed()
			}
			else -> {
				lastBackPress = System.currentTimeMillis()
				toast("Press again to confirm exit")
			}
		}
	}

	override fun onResume() {
		super.onResume()
		Intent(MainApplication.instance, PlaybackService::class.java).let {
			it.action = PlaybackService.Companion.Action.STARTFOREGROUND_ACTION
			MainApplication.instance.startService(it)
		}
		ShakeDetector.onShakeListener = object: ShakeDetector.OnShakeListener {
			override fun onShake() {
				val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
				// Vibrate for 500 milliseconds
				if (Build.VERSION.SDK_INT >= 26) {
					v.vibrate(VibrationEffect.createOneShot(150,255))
				} else {
					v.vibrate(150)
				}
				showConfirmDialog(
					this@PaletteEditorActivity,
					"Erase everything to start from scratch?"
				) {
					viewModel.palette = Palette()
				}
			}
		}
	}

	override fun onPause() {
		super.onPause()
		AudioTrackCache.releaseAll()
		//ui.sequencerThread.stopped = true
		Storage.storePalette(viewModel.palette, this)
		ShakeDetector.onShakeListener = null
	}

	override fun onStop() {
		super.onStop()
		Storage.storePalette(viewModel.palette, this)
	}

	override fun onDestroy() {
		super.onDestroy()
		BeatClockPaletteConsumer.viewModel = null
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		ui.sequencerInstrument.instrument = savedInstanceState.getByte("sequencerInstrument", GeneralMidiConstants.SYNTH_BASS_1)
		if (savedInstanceState.getBoolean("pianoHidden")) {
			viewModel.keyboardView.hide(animated = false)
		}
		if (savedInstanceState.getBoolean("melodyHidden")) {
			viewModel.colorboardView.hide(animated = false)
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Storage.storePalette(viewModel.palette, this)
		outState.putBoolean("pianoHidden", viewModel.keyboardView.isHidden)
		outState.putBoolean("melodyHidden", viewModel.colorboardView.isHidden)
	}
}