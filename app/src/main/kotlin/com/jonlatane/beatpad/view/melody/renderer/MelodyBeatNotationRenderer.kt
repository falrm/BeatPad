package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.CanvasToneDrawer
import org.jetbrains.anko.warn
import org.jetbrains.anko.withAlpha


interface MelodyBeatNotationRenderer: BaseMelodyBeatRenderer, CanvasToneDrawer {
  val notationAlpha: Float
  val filledNotehead: Drawable
  

  fun renderNotationMelodyBeat(canvas: Canvas) {
    canvas.renderStaffLines()
    melody?.let { melody ->
      canvas.drawNotatedMelody(
        melody,
        stepNoteAlpha = 255,
        drawRhythm = false,
        drawColorGuide = false,
        forceDrawColorGuideForCurrentBeat = true
      )
    }


    BeatClockPaletteConsumer.section?.let { section ->
      section.melodies.filter { !it.isDisabled }.map { it.melody }.forEach { melody ->
        canvas.drawNotatedMelody(
          melody,
          stepNoteAlpha = 22,
          drawColorGuide = false
        )
      }
    }
  }


  fun Canvas.drawNotatedMelody(
    melody: Melody<*>,
    stepNoteAlpha: Int,
    drawRhythm: Boolean = false,
    drawColorGuide: Boolean = true,
    forceDrawColorGuideForCurrentBeat: Boolean = false
  ) {
    iterateSubdivisions(melody) { elementIndex, elementPosition ->
      val isCurrentlyPlayingBeat: Boolean =
        viewModel.paletteViewModel.playbackTick?.convertPatternIndex(
          from = BeatClockPaletteConsumer.ticksPerBeat,
          to = melody
        ) == elementPosition
      colorGuideAlpha = (when {
        !drawColorGuide -> when {
          forceDrawColorGuideForCurrentBeat && isCurrentlyPlayingBeat -> 119
          else -> 0
        }
        melody.limitedToNotesInHarmony -> when {
          isCurrentlyPlayingBeat -> 255
          else -> 187
        }
        isCurrentlyPlayingBeat         -> 119
        else                           -> 0
      } * notationAlpha).toInt()
      if(colorGuideAlpha > 0) {
        drawColorGuide()
      }
      drawNotatedStepNotes(melody, elementPosition, stepNoteAlpha, notationAlpha)
      if(drawRhythm) drawRhythm(melody, elementIndex, notationAlpha)
    }

    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      left = overallWidth
      right = overallWidth
    }
    if(drawRhythm) drawRhythm(melody, melody.subdivisionsPerBeat, notationAlpha)
  }



  fun Canvas.drawNotatedStepNotes(
    melody: Melody<*>,
    elementPosition: Int,
    drawAlpha: Int = 0xFF,
    alphaSource: Float
  ) {
    val element: Transposable<*>? = melody.changes[elementPosition]
    val nextElement: Transposable<*>? = melody.changes[elementPosition]
    val isChange = element != null
    paint.color = (if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()).withAlpha((alphaSource * drawAlpha).toInt())

    try {
      val tones: Set<Int> = melody.changeBefore(elementPosition).let {
        (it as? RationalMelody.Element)?.tones
      } ?: emptySet()

      if (tones.isNotEmpty()) {
        val leftMargin = if (isChange) drawPadding else 0
        val rightMargin = if (nextElement != null) drawPadding else 0
        tones.forEach { tone ->
          val chosenTone = if(melody.limitedToNotesInHarmony) {
            val transposedTone = tone + melody.offsetUnder(chord)
            chord.closestTone(transposedTone)
          } else tone + melody.offsetUnder(chord)
//          val top = height - height * (chosenTone - AlphaDrawer.BOTTOM) / 88f
//          val bottom = height - height * (chosenTone - AlphaDrawer.BOTTOM + 1) / 88f
//          val center = (top + bottom) / 2
          val chosenNote = Note.naturalOrSharpNoteFor(chosenTone)
          val center = pointFor(chosenNote)

          val notehead = filledNotehead.constantState.newDrawable().mutate()
            .apply { alpha = (drawAlpha * alphaSource).toInt() }
          val boundsWidth = bounds.width()
          val noteheadWidth = Math.min((letterStepSize * 2).toInt(), boundsWidth)
          val noteheadHeight = noteheadWidth//(bounds.right - bounds.left)
          notehead.setBounds(
            bounds.right - noteheadWidth,
            center.toInt() - (noteheadHeight / 2),
            bounds.right,
            center.toInt() + (noteheadHeight / 2)
          )
          notehead.alpha = (drawAlpha * alphaSource).toInt()
          notehead.draw(this)
        }
      }
    } catch (t: Throwable) {
      warn("Error drawing pressed notes in sequence", t)
      invalidateDrawingLayer()
    }
  }

  fun Canvas.renderLedgerLines(tone: Int) {

  }

  fun Canvas.centerOfTone(tone: Int): Float
    = startPoint - (bottomMostNote + tone - 9.5f) * halfStepWidth

  fun Canvas.renderStaffLines() {
    paint.color = color(R.color.colorPrimaryDark).withAlpha((255 * notationAlpha).toInt())
    //val halfStepWidth: Float = axisLength / halfStepsOnScreen
    listOf(Clef.TREBLE, Clef.BASS).flatMap { it.notes }.forEach{ note ->
      renderLineAt(
        pointFor(letter = note.letter, octave = note.octave)
      )
    }
  }
  fun Canvas.pointFor(note: Note): Float
    = pointFor(note.letter, note.octave)

  fun Canvas.pointFor(letter: Note.Letter, octave: Int): Float {
    val middleC = centerOfTone(0)
    val result = middleC - letterStepSize * (((octave - 4) * 7) + letter.letterOffset)
    return result
  }

  val letterStepSize: Float get() = halfStepWidth * 12f / 7f

  fun Canvas.renderLineAt(pointOnToneAxis: Float)  {
    if (renderVertically) {
      drawLine(
        bounds.left.toFloat(),
        pointOnToneAxis,
        bounds.right.toFloat(),
        pointOnToneAxis,
        paint
      )
    } else {
      drawLine(
        pointOnToneAxis,
        bounds.top.toFloat(),
        pointOnToneAxis,
        bounds.bottom.toFloat(),
        paint
      )
    }
  }


}