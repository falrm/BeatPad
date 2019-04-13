package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.view.colorboard.CanvasToneDrawer
import org.jetbrains.anko.warn
import org.jetbrains.anko.withAlpha
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min


interface MelodyBeatNotationRenderer: BaseMelodyBeatRenderer, CanvasToneDrawer {
  val notationAlpha: Float
  fun createFilledNotehead(): Drawable
  fun flushNotationDrawableCache()
  val sharp: Drawable
  val clefs: List<Clef> get() = listOf(Clef.TREBLE, Clef.BASS)

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
      section.melodies.filter { !it.isDisabled }.filter {
        when(melody?.limitedToNotesInHarmony) {
          null -> false
          true -> it.melody.limitedToNotesInHarmony
          false -> !it.melody.limitedToNotesInHarmony
        }
      }.map { it.melody }.forEach { melody ->
        canvas.drawNotatedMelody(
          melody,
          stepNoteAlpha = 22,
          drawColorGuide = false
        )
      }
    }
    flushNotationDrawableCache()
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
        val playbackNotes = tones.map {
          val playbackTone = melody.playbackToneUnder(it, chord)
          Note.naturalOrSharpNoteFor(playbackTone)
        }
        playbackNotes.forEach { note ->
          val center = pointFor(note)

          val notehead = createFilledNotehead().constantState.newDrawable().mutate()
            .apply { alpha = (drawAlpha * alphaSource).toInt() }
          val boundsWidth = bounds.width()
          val maxFittableNoteheadWidth = ceil(boundsWidth / 2.3f).toInt()
          val noteheadWidth = Math.min((letterStepSize * 2).toInt(), maxFittableNoteheadWidth)
          val noteheadHeight = noteheadWidth//(bounds.right - bounds.left)
          val shouldStagger = playbackNotes.any {
            it.heptatonicValue % 2 == 0
              && (
              it.heptatonicValue == note.heptatonicValue + 1
                || it.heptatonicValue == note.heptatonicValue - 1
              )
          }
          val top = center.toInt() - (noteheadHeight / 2)
          val bottom = center.toInt() + (noteheadHeight / 2)
          val (left, right) = if(shouldStagger) {
            bounds.right - noteheadWidth to bounds.right
          } else {
            (bounds.right - 1.6f * noteheadWidth).toInt() to (bounds.right - .6f * noteheadWidth).toInt()
          }
          notehead.setBounds(left, top, right, bottom)
          notehead.alpha = (drawAlpha * alphaSource).toInt()
          notehead.draw(this)

          if(note.sign == Note.Sign.Sharp) {
            val sharp = sharp.constantState.newDrawable().mutate()
              .apply { alpha = (drawAlpha * alphaSource).toInt() }

            val (sharpLeft, sharpRight) = if(shouldStagger) {
              (bounds.right - 2.1f * noteheadWidth).toInt() to (bounds.right - 1.6f * noteheadWidth).toInt()
            } else {
              (bounds.right - 2.3f * noteheadWidth).toInt() to (bounds.right - 1.8f * noteheadWidth).toInt()
            }
            sharp.setBounds(sharpLeft, top, sharpRight, bottom)
            sharp.alpha = (drawAlpha * alphaSource).toInt()
            sharp.draw(this)
          }
          renderLedgerLines(note, left, right)
        }
      }
    } catch (t: Throwable) {
      warn("Error drawing pressed notes in sequence", t)
      invalidateDrawingLayer()
    }
  }

  fun Canvas.renderLedgerLines(note: Note, left: Int, right: Int) {
    if (!clefs.any { it. covers(note) }) {
      val nearestClef = clefs.minBy {
        min(
          abs(note.heptatonicValue - it.heptatonicMax),
          abs(note.heptatonicValue - it.heptatonicMin)
        )
      }!!
      nearestClef.ledgersTo(note).forEach { ledger ->
        renderLineAt(
          pointFor(letter = ledger.letter, octave = ledger.octave),
          left.toFloat() - dip(3),
          right.toFloat() + dip(3)
        )
      }
    }
  }

  fun Canvas.centerOfTone(tone: Int): Float
    = startPoint - (bottomMostNote + tone - 9.5f) * halfStepWidth

  fun Canvas.renderStaffLines() {
    paint.color = color(R.color.colorPrimaryDark).withAlpha((255 * notationAlpha).toInt())
    //val halfStepWidth: Float = axisLength / halfStepsOnScreen
    clefs.flatMap { it.notes }.forEach{ note ->
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

  fun Canvas.renderLineAt(
    pointOnToneAxis: Float,
    left: Float? = null,
    right: Float? = null
  )  {
    val stroke = paint.strokeWidth
    paint.strokeWidth = 2f
    if (renderVertically) {
      drawLine(
        left ?: bounds.left.toFloat(),
        pointOnToneAxis,
        right ?: bounds.right.toFloat(),
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
    paint.strokeWidth = stroke
  }


}