package com.jonlatane.beatpad.view.melody

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.button

/**
 * A [NonDelayedHorizontalScrollView] that tracks if the user is holding it down.
 */
class MelodyEditingModifiers @JvmOverloads constructor(
	context: Context
) : _LinearLayout(context) {
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
				  field == Modifier.None -> { onHeldDownChanged?.invoke(true) }
				  value == Modifier.None -> { onHeldDownChanged?.invoke(false) }
			  }
		  }
		  field = value
	  }
	val isHeldDown get() = modifier == Modifier.None
	var onHeldDownChanged: ((Boolean) -> Unit)? = null
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

	fun View.animateWeight(height: Float, duration: Long = 300L) {
		val anim = ValueAnimator.ofFloat(this.layoutWeight, height)
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
		}.lparams {
			weight = 1f
		}
		articulateButton = button {
			text = "Articulate"
		}.lparams {
			weight = 1f
		}
		transposeButton = button {
			text = "Transpose"
		}.lparams {
			weight = 1f
		}

		val activePointers: MutableMap<Button, MutableSet<Int>> = mutableMapOf()
		arrayOf(editButton, articulateButton, transposeButton).forEach { button ->
			activePointers[button] = mutableSetOf()
			button.setOnTouchListener( {
				_, ev ->
				when(ev.actionMasked)  {
					MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
						// We have a new pointer. Lets add it to the list of pointers
						modifier = modifierOf(button)
						button.animateWeight(20f)
					}
					MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
						modifier = Modifier.None
						button.animateWeight(1f)
					}
				}
				true
			})
		}
	}
}
