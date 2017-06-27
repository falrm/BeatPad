package com.jonlatane.beatpad.view.melody

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.jonlatane.beatpad.util.HideableView
import org.jetbrains.anko.AnkoLogger

abstract class BaseMelodyView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : View(context, attrs, defStyle), AnkoLogger {

}