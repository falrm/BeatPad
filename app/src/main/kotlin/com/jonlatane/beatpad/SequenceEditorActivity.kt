package com.jonlatane.beatpad

import android.app.Activity
import android.os.Bundle
import com.jonlatane.beatpad.view.tonesequence.ToneSequenceUI
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.setContentView

class SequenceEditorActivity : Activity(), AnkoLogger {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		info("hi hi hi hi")
		ToneSequenceUI().setContentView(this)
	}
}