package com.jonlatane.beatpad.view.colorboard

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.melody.MelodyBeatView

interface CanvasToneDrawer : AlphaDrawer {
  val showSteps: Boolean
  val chord: Chord

  // Renders the dividers that separate A, A#, B, C, etc. visually to the user
  fun Canvas.renderSteps() {
    paint.color = color(R.color.colorPrimaryDark)
    if (showSteps) {
      val halfStepWidth: Float = axisLength / halfStepsOnScreen
      var linePosition = onScreenNotes.first().top - 12 * halfStepWidth //TODO gross hack
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

  /** Renders the lines for G2, B2, D3, F3, A3 (bass clef) and E4, G4, B4, D5, F5 (treble clef) */
  fun Canvas.renderGrandStaffLines() {
    paint.color = color(R.color.colorPrimaryDark)
    //TODO implement this.
  }


  data class OnScreenNote(
    var tone: Int = 0,
    var pressed: Boolean = false,
    var bottom: Float = 0f,
    var top: Float = 0f,
    var center: Float = 0f
  )

  data class VisiblePitch(
    var tone: Int,
    var bounds: RectF = RectF()
  )

  val visiblePitches: List<VisiblePitch> get() {
    val result = mutableListOf<VisiblePitch>()
    val orientationRange = highestPitch - lowestPitch + 1 - halfStepsOnScreen
    // This "point" is under the same scale as bottomMostNote; i.e. 0.5f is a "quarter step"
    // (in scrolling distance) past middle C, regardless of the scale level.
    val bottomMostPoint: Float = lowestPitch + (Orientation.normalizedDevicePitch() * orientationRange)
    val bottomMostNote: Int = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
    val halfStepPhysicalDistance = axisLength / halfStepsOnScreen
    for (toneMaybeNotInChord in (bottomMostNote..(bottomMostNote + halfStepsOnScreen.toInt() + 2))) {
      result += VisiblePitch(toneMaybeNotInChord).apply {
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

  val onScreenNotes: List<OnScreenNote>
    get() {
      val result = mutableListOf<OnScreenNote>()
      val orientationRange = highestPitch - lowestPitch + 1 - halfStepsOnScreen
      // This "point" is under the same scale as bottomMostNote; i.e. 0.5f is a "quarter step"
      // (in scrolling distance) past middle C, regardless of the scale level.
      val bottomMostPoint: Float = lowestPitch + (Orientation.normalizedDevicePitch() * orientationRange)
      val bottomMostNote: Int = java.lang.Math.floor(bottomMostPoint.toDouble()).toInt()
      val halfStepPhysicalDistance = axisLength / halfStepsOnScreen
      val startPoint = (bottomMostNote - bottomMostPoint) * halfStepPhysicalDistance
      var currentScreenNote = OnScreenNote(
        tone = chord.closestTone(bottomMostNote),
        pressed = false,
        bottom = 0f,
        center = 0f,
        top = (bottomMostNote - bottomMostPoint) * halfStepPhysicalDistance
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