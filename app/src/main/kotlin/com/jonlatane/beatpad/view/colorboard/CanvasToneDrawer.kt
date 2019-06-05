package com.jonlatane.beatpad.view.colorboard

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.sensors.Orientation
import kotlinx.io.pool.DefaultPool
import java.util.*

interface CanvasToneDrawer : AlphaDrawer {
  fun dip(value: Float): Int
  fun dip(value: Int): Int
  val showSteps: Boolean
  val chord: Chord

  // Renders the dividers that separate A, A#, B, C, etc. visually to the user
  fun Canvas.renderSteps() {
    paint.color = color(R.color.colorPrimaryDark)
    if (showSteps) {
      //val halfStepWidth: Float = axisLength / halfStepsOnScreen
      var linePosition = startPoint - 12 * halfStepWidth
      while (linePosition < axisLength) {
        if (renderVertically) {
          drawLine(
            bounds.left.toFloat(),
            linePosition,
            bounds.right.toFloat(),
            linePosition,
            Paint()
          )
        } else {
          drawLine(
            linePosition,
            bounds.top.toFloat(),
            linePosition,
            bounds.bottom.toFloat(),
            paint
          )
        }
        linePosition += halfStepWidth
      }
    }
  }

  data class OnScreenNote(
    var tone: Int = 0,
    var pressed: Boolean = false,
    var bottom: Float = 0f,
    var top: Float = 0f,
    var center: Float = 0f
  )

  companion object {
    private val visiblePitchPool: DefaultPool<VisiblePitch> = object : DefaultPool<VisiblePitch>(16) {
      override fun produceInstance() = VisiblePitch()
    }

    private val visiblePitchListPool: DefaultPool<MutableList<VisiblePitch>> = object : DefaultPool<MutableList<VisiblePitch>>(16) {
      override fun produceInstance() = Collections.synchronizedList(mutableListOf<VisiblePitch>())
      override fun validateInstance(instance: MutableList<VisiblePitch>) {
        super.validateInstance(instance)
        instance.clear()
      }
    }
  }

  data class VisiblePitch(
    var tone: Int = 0,
    var bounds: RectF = RectF()
  )

  val visiblePitches: List<VisiblePitch> get() {
    val result = visiblePitchListPool.borrow()
    val orientationRange = highestPitch - lowestPitch + 1 - halfStepsOnScreen
    // This "point" is under the same scale as bottomMostNote; i.e. 0.5f is a "quarter step"
    // (in scrolling distance) past middle C, regardless of the scale level.
    val bottomMostPoint: Float = lowestPitch + (Orientation.normalizedDevicePitch() * orientationRange)
    val bottomMostNote: Int = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
    val halfStepPhysicalDistance = axisLength / halfStepsOnScreen
    for (toneMaybeNotInChord in (bottomMostNote..(bottomMostNote + halfStepsOnScreen.toInt() + 2))) {
      result += visiblePitchPool.borrow().apply {
        tone = toneMaybeNotInChord
        bounds.apply {

          if(renderVertically) {
            left = this@CanvasToneDrawer.bounds.left.toFloat()
            top = this@CanvasToneDrawer.bounds.bottom -
              (tone.toFloat() - bottomMostPoint) * halfStepPhysicalDistance
            right = this@CanvasToneDrawer.bounds.right.toFloat()
            bottom = this@CanvasToneDrawer.bounds.bottom -
              (1 + tone.toFloat() - bottomMostPoint) * halfStepPhysicalDistance
          } else {
            left = this@CanvasToneDrawer.bounds.left.toFloat() +
              (tone.toFloat() - bottomMostPoint) * halfStepPhysicalDistance
            top = this@CanvasToneDrawer.bounds.top.toFloat()
            right = this@CanvasToneDrawer.bounds.left.toFloat() +
              (1 + tone.toFloat() - bottomMostPoint) * halfStepPhysicalDistance
            bottom = this@CanvasToneDrawer.bounds.bottom .toFloat()
          }
        }
      }
    }
    return result
  }

  val orientationRange: Float get() = highestPitch - lowestPitch + 1 - halfStepsOnScreen
  val bottomMostPoint: Float get() = lowestPitch + (Orientation.normalizedDevicePitch() * orientationRange)

  val bottomMostNote: Int get() = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
  val halfStepPhysicalDistance: Float get() = axisLength / halfStepsOnScreen
  val startPoint: Float get() = (bottomMostNote - bottomMostPoint) * halfStepPhysicalDistance
  val onScreenNotes: Iterable<OnScreenNote>
    get() {
      val result = mutableListOf<OnScreenNote>()
      // This "point" is under the same scale as bottomMostNote; i.e. 0.5f is a "quarter step"
      // (in scrolling distance) past middle C, regardless of the scale level.
      val bottomMostNote: Int = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
      val halfStepPhysicalDistance = axisLength / halfStepsOnScreen
      val startPoint = (bottomMostNote - bottomMostPoint) * halfStepPhysicalDistance
      var currentScreenNote = OnScreenNote(
        tone = chord.closestTone(bottomMostNote),
        pressed = false,
        bottom = 0f,
        center = 0f,
        top = startPoint
      )
      for (toneMaybeNotInChord in (bottomMostNote..(bottomMostNote + halfStepsOnScreen.toInt() + 2))) {
        val toneInChord = chord.closestTone(toneMaybeNotInChord)
        if(toneInChord == toneMaybeNotInChord) {
          currentScreenNote.center = currentScreenNote.top + (0.5f * halfStepPhysicalDistance)
        }
        if (toneInChord != currentScreenNote.tone) {
          result.add(currentScreenNote)
          currentScreenNote = OnScreenNote(
            tone = toneInChord,
            pressed = false,
            bottom = currentScreenNote.top,
            top = currentScreenNote.top,
            center = currentScreenNote.top + (0.5f * halfStepPhysicalDistance)
          )
        }
        currentScreenNote.top += halfStepPhysicalDistance
      }
      result.add(currentScreenNote)
      return result
    }
}