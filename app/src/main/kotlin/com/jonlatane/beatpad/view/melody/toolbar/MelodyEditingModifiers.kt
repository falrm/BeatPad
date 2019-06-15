package com.jonlatane.beatpad.view.melody.toolbar

import android.animation.ValueAnimator
import android.content.Context
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.animateWidth
import com.jonlatane.beatpad.util.layoutWidth
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.HideableLinearLayout
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import org.jetbrains.anko.*

/**
 * A [NonDelayedHorizontalScrollView] that tracks if the user is holding it down.
 */
class MelodyEditingModifiers @JvmOverloads constructor(
	context: Context
) : HideableLinearLayout(context) {
  companion object {
    const val vibrationMs = 10
  }
	sealed class Modifier {
		object None: Modifier()
		object Editing: Modifier()
		object Articulating: Modifier()
		object Transposing: Modifier()
	}
	var modifier: Modifier = Modifier.None
	  private set(value) {
		  if(field != value) {
			  when {
				  field == Modifier.None -> { onHeldDownChanged(true) }
				  value == Modifier.None -> { onHeldDownChanged(false) }
			  }
		  }
		  field = value
	  }
	var onHeldDownChanged: ((Boolean) -> Unit) = {}
	private val editButton: Button
	private val articulateButton: Button
	private val transposeButton: Button

	private fun modifierOf(button: Button) = when (button) {
		editButton -> Modifier.Editing
		articulateButton -> Modifier.Articulating
		transposeButton -> Modifier.Transposing
		else -> TODO("Impossible condition")
	}

	var View.layoutWeight get() = (this.layoutParams as LinearLayout.LayoutParams).weight
		set(value) {
			val layoutParams = (this.layoutParams as LinearLayout.LayoutParams)
			layoutParams.weight = value
			this.layoutParams = layoutParams
		}

	fun View.animateWeight(weight: Float, duration: Long = 300L) {
		val anim = ValueAnimator.ofFloat(this.layoutWeight, weight)
		anim.interpolator = LinearInterpolator()
		anim.addUpdateListener { valueAnimator ->
			val value = valueAnimator.animatedValue as Float
			this.layoutWeight = value
		}
		anim.setDuration(duration).start()
	}

	init {
		orientation = HORIZONTAL
		editButton = button {
			text = "Edit"
			singleLine = true
      allCaps = true
			ellipsize = TextUtils.TruncateAt.END
			typeface = MainApplication.chordTypefaceBold
		}.lparams {
//			width = matchParent
			weight = 1f
      width = 0
      height = matchParent
		}
		articulateButton = button {
			text = "Articulate"
			singleLine = true
      allCaps = true
			ellipsize = TextUtils.TruncateAt.END
			typeface = MainApplication.chordTypefaceBold
		}.lparams {
			//width = matchParent
			weight = 1f
      width = 0
      height = matchParent
		}
		transposeButton = button {
			text = "Transpose"
			singleLine = true
      allCaps = true
			ellipsize = TextUtils.TruncateAt.END
			typeface = MainApplication.chordTypefaceBold
		}.lparams {
			//width = matchParent
			weight = 1f
      width = 0
      height = matchParent
		}


    var restoredWidths = mutableMapOf<Button, Int>()
		val activePointers: MutableMap<Button, MutableSet<Int>> = mutableMapOf()
		arrayOf(editButton, articulateButton, transposeButton).forEach { button ->
			val otherButtons = listOf(editButton, articulateButton, transposeButton) - button
			activePointers[button] = mutableSetOf()
			button.setOnTouchListener { _, ev ->
				when(ev.actionMasked)  {
					MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
						modifier = modifierOf(button)
						otherButtons.forEach {
							it.isEnabled = false
							it.layoutWidth = it.measuredWidth
              restoredWidths[it] = restoredWidths[it] ?: it.measuredWidth
							it.layoutWeight = 0f
							it.animateWidth(0)
						}
						//button.animateWeight(20f)
						vibrate(vibrationMs)
					}
					MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
						modifier = Modifier.None
            otherButtons.forEach {
							it.layoutWeight = 0f
							it.animateWidth(restoredWidths[it]!!, endAction = {
                if(modifier == Modifier.None) {
                  it.layoutWeight = 1f
                  it.layoutWidth = 0
                  it.isEnabled = true
                }
							})
						}
						//button.animateWeight(1f)
						vibrate(vibrationMs)
					}
				}
				true
			}
		}
	}
}
