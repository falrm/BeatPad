package com.jonlatane.beatpad.view.palette

import android.app.Dialog
import android.content.Context
import android.view.ViewManager
import android.widget.LinearLayout
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.hide
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.util.show
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.content.Intent
import android.widget.Button
import android.widget.NumberPicker
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.output.service.BeatClockProducer
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.view.tempo.TempoTracking
import org.jetbrains.anko.sdk25.coroutines.onLongClick

class PaletteToolbar(ctx: Context,
                     val viewModel: PaletteViewModel) : _LinearLayout(ctx), AnkoLogger {
	lateinit var tempoTapper: Button
	init {
		orientation = LinearLayout.HORIZONTAL
		backgroundColor = context.color(R.color.colorPrimaryDark)

		button {
			text = "Play"
			onClick {
				val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
				startIntent.action = PlaybackService.Companion.Action.PLAY_ACTION
				MainApplication.instance.startService(startIntent)
			}
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}

		button {
			text = "Stop"
			onClick {
				val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
				startIntent.action = PlaybackService.Companion.Action.PAUSE_ACTION
				MainApplication.instance.startService(startIntent)
			}
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}

		tempoTapper = button {
			text = ""
			onLongClick {
				showTempoPicker()
			}
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}
		TempoTracking.trackTempo(tempoTapper) { tempo: Float ->
			info("onTempoChanged: $tempo")
			val bpm = Math.round(tempo)
			if (bpm > 20) {
				BeatClockProducer.bpm = bpm
				updateTempoButton()
			}
		}
		updateTempoButton()

		button {
			text = "Keys"
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}.onClick {
				if(viewModel.keyboardView.isHidden)
					viewModel.keyboardView.show()
				else
					viewModel.keyboardView.hide()
			}

		button {
			text = "Colors"
		}.lparams {
			width = matchParent
			height = wrapContent
			weight = 1f
		}.onClick {
				if(viewModel.colorboardView.isHidden)
					viewModel.colorboardView.show()
				else
					viewModel.colorboardView.hide()
		}
	}



	private fun showTempoPicker() {
		val dialog = Dialog(context)
		dialog.setTitle("Select Tempo")
		dialog.setContentView(R.layout.dialog_choose_tempo)
		val picker = dialog.findViewById<NumberPicker>(R.id.numberPicker1)
		picker.maxValue = 960
		picker.minValue = 15
		picker.value = BeatClockProducer.bpm
		picker.wrapSelectorWheel = false
		picker.setOnValueChangedListener { _, _, _ ->
			val bpm = picker.value
			BeatClockProducer.bpm = bpm
			updateTempoButton()
		}
		dialog.show()
	}

	private fun updateTempoButton() {
		tempoTapper.text = "${BeatClockProducer.bpm}BPM"
	}
}
