package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.view.colorboard.CanvasToneDrawer
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.warn
import org.jetbrains.anko.withAlpha
import java.util.*
import kotlin.math.*


interface MelodyBeatNotationRenderer : BaseMelodyBeatRenderer, MelodyBeatRhythmRenderer {
  val notationAlpha: Float

  fun renderNotationMelodyBeat(canvas: Canvas) {
    paint.color = color(android.R.color.black).withAlpha((255 * notationAlpha).toInt())
    paint.strokeWidth = (bounds.width() * 0.008f).let {
      val minValue = 1f
      when {
        it > minValue -> round(it)
        else          -> minValue
      }
    }
    canvas.renderStaffLines()

    melody?.let { melody ->
      val alphaMultiplier = if (viewModel.isMelodyReferenceEnabled) 1f else 2f / 3
      canvas.drawNotatedMelody(
        melody,
        drawAlpha = notationAlpha * alphaMultiplier,
        drawRhythm = false,
        drawColorGuide = false,
        forceDrawColorGuideForCurrentBeat = true
      )
    }

    if(isFinalBeat) {
      canvas.drawHorizontalLineInBounds(
        leftSide = false,
        strokeWidth = paint.strokeWidth * 2,
        startY = canvas.pointFor(clefs.flatMap { it.notes }.maxBy { it.heptatonicValue }!!),
        stopY = canvas.pointFor(clefs.flatMap { it.notes }.minBy { it.heptatonicValue }!!)
      )
    }

    paint.color = color(android.R.color.black).withAlpha((255 * notationAlpha / 3f).toInt())
    sectionMelodiesOfPartType.filter { it != melody }.forEach { melody ->
      canvas.drawNotatedMelody(
        melody,
        drawAlpha = notationAlpha / 3f,
        drawColorGuide = false,
        stemsUp = false
      )
    }
    flushNotationDrawableCache()
  }

  override val harmony: Harmony get() = viewModel.harmony!!
  val meter: Harmony.Meter get() = harmony.meter
  val isFinalBeat: Boolean get() = viewModel.harmony?.meter?.let { meter ->
      //TODO make this reflect all the possibilities of [Harmony.Meter]
      (beatPosition + 1) % meter.defaultBeatsPerMeasure == 0
    } ?: false

  fun flushNotationDrawableCache() {
    listOf(filledNoteheadPool, sharpPool, xNoteheadPool)
      .forEach { it.flushNotationDrawableCache() }
  }

  val sharpPool: DrawablePool
  val flatPool: DrawablePool
  val naturalPool: DrawablePool
  val filledNoteheadPool: DrawablePool
  val xNoteheadPool: DrawablePool

  class DrawablePool(val drawableResource: Int, val context: Context) : DefaultPool<Drawable>(15) {
    val notationDrawableCache = Collections.synchronizedList(mutableListOf<Drawable>())
    override fun produceInstance(): Drawable {
      val result = context.resources.getDrawable(drawableResource, null)
        ?.constantState?.newDrawable()?.mutate()
      notationDrawableCache.add(result)
      return result!!
    }

    fun flushNotationDrawableCache() {
      notationDrawableCache.removeAll {
        recycle(it); true
      }
    }
  }

  val clefs: List<Clef> get() = listOf(Clef.TREBLE, Clef.BASS)

  val sectionMelodies
    get() = BeatClockPaletteConsumer.section?.let { section ->
      section.melodies.filter { !it.isDisabled }
    }?.map { it.melody } ?: emptyList()

  val sectionMelodiesOfPartType
    get() = arrayOf(melody).filterNotNull() +
      sectionMelodies.filter {
        when (melody?.limitedToNotesInHarmony) {
          null  -> false
          true  -> it.limitedToNotesInHarmony
          false -> !it.limitedToNotesInHarmony
        }
      }


  fun Canvas.drawNotatedMelody(
    melody: Melody<*>,
    drawAlpha: Float = notationAlpha,
    drawRhythm: Boolean = false,
    drawColorGuide: Boolean = true,
    forceDrawColorGuideForCurrentBeat: Boolean = false,
    stemsUp: Boolean = true
  ) {
    val maxSubdivisonsPerBeat = (sectionMelodiesOfPartType + melody)
      .map { it.subdivisionsPerBeat }.max()!!
    val maxBoundsWidth = (overallBounds.right - overallBounds.left) / maxSubdivisonsPerBeat
    iterateSubdivisions(melody) { elementPosition ->
      bounds.right = bounds.left + maxBoundsWidth
      val isCurrentlyPlayingBeat: Boolean =
        viewModel.paletteViewModel.playbackTick?.convertPatternIndex(
          fromSubdivisionsPerBeat = BeatClockPaletteConsumer.ticksPerBeat,
          toSubdivisionsPerBeat = melody.subdivisionsPerBeat
        ) == elementPosition
      colorGuideAlpha = (when {
        !drawColorGuide                -> when {
          forceDrawColorGuideForCurrentBeat && isCurrentlyPlayingBeat -> 119
          else                                                        -> 0
        }
        melody.limitedToNotesInHarmony -> when {
          isCurrentlyPlayingBeat -> 255
          else                   -> 187
        }
        isCurrentlyPlayingBeat         -> 119
        else                           -> 0
      } * drawAlpha).toInt()
      if (colorGuideAlpha > 0) {
        drawColorGuide()
      }
      drawNoteheadsLedgersAndStems(melody, harmony, elementPosition, drawAlpha, stemsUp)
      if (drawRhythm) drawRhythm(melody, elementPosition, drawAlpha)
    }

    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      left = overallWidth
      right = overallWidth
    }
    if (drawRhythm) drawRhythm(melody, melody.subdivisionsPerBeat, drawAlpha)
  }

  fun previousSignOf(
    melody: Melody<*>,
    harmony: Harmony,
    note: Note,
    melodyPosition: Int
  ): Note.Sign? {
    val currentBeatPosition = melodyPosition.toFloat() / melody.subdivisionsPerBeat
    val lastDownbeat = (0..Int.MAX_VALUE).first {
      it % harmony.meter.defaultBeatsPerMeasure == 0 &&
      it <= currentBeatPosition && it + harmony.meter.defaultBeatsPerMeasure > currentBeatPosition
    }
    var result: Note.Sign? = null
    melody.changes.navigableKeySet()
      .subSet(lastDownbeat * melody.subdivisionsPerBeat, true, melodyPosition, true)
      .reversed()
      .any { changeIndex ->
        val change = melody.changes[changeIndex]
        val tones: Set<Int> = (change.let {
          (it as? RationalMelody.Element)?.tones
        } ?: emptySet<Int>()).filter {
          !(changeIndex == melodyPosition && note.tone == it)
        }.toSet()

        val chordAtTheTime = chordAt(changeIndex)!!
        val playbackNotes = tones.map {
          val playbackTone = melody.playbackToneUnder(it, chordAtTheTime)
          Note.nameNoteUnderChord(playbackTone, chordAtTheTime)
        }
        playbackNotes.firstOrNull { it.heptatonicValue == note.heptatonicValue }?.also {
          result = it.sign
        } != null
      }
    return result
  }



  fun Canvas.drawNoteheadsLedgersAndStems(
    melody: Melody<*>,
    harmony: Harmony,
    elementPosition: Int,
    //drawAlpha: Int = 0xFF,
    alphaSource: Float,
    stemsUp: Boolean = true
  ) {
    val element: Transposable<*>? = melody.changes[elementPosition % melody.length]
    val nextElement: Transposable<*>? = melody.changes[elementPosition % melody.length]
    val isChange = element != null
    //paint.color = (if (isChange) 0xAA212121.toInt() else 0xAA424242.toInt()).withAlpha((alphaSource * drawAlpha).toInt())

    try {
      val tones: Set<Int> = melody.changeBefore(elementPosition % melody.length).let {
        (it as? RationalMelody.Element)?.tones
      } ?: emptySet()

      if (tones.isNotEmpty()) {
        val boundsWidth = bounds.width()
        val maxFittableNoteheadWidth = ceil(boundsWidth / 2.6f).toInt()
        val noteheadWidth = Math.min((letterStepSize * 2).toInt(), maxFittableNoteheadWidth)
        val noteheadHeight = noteheadWidth//(bounds.right - bounds.left)

        val playbackNotes = tones.map {
          val playbackTone = melody.playbackToneUnder(it, chord)
          Note.nameNoteUnderChord(playbackTone, chord)
        }
        var maxCenter = Float.MIN_VALUE
        var minCenter = Float.MAX_VALUE
        var hadStaggeredNotes = false
        var minWasStaggered = false
        var maxWasStaggered = false
        playbackNotes.forEach { note ->
          val center = pointFor(note)
          minCenter = min(center, minCenter)
          maxCenter = max(center, maxCenter)

          val notehead = when {
            melody.limitedToNotesInHarmony -> filledNoteheadPool.borrow()
            else                           -> xNoteheadPool.borrow()
          }.apply { alpha = (255 * alphaSource).toInt() }
          val shouldStagger = playbackNotes.any {
            (
              it.heptatonicValue % 2 == 0
                && (
                it.heptatonicValue == note.heptatonicValue + 1
                  || it.heptatonicValue == note.heptatonicValue - 1
                )
              ) || (
              it.heptatonicValue == note.heptatonicValue
                && it.tone > note.tone
              )
          }
          hadStaggeredNotes = hadStaggeredNotes || shouldStagger
          if (minCenter == center) minWasStaggered = shouldStagger
          if (maxCenter == center) maxWasStaggered = shouldStagger
          val top = center.toInt() - (noteheadHeight / 2)
          val bottom = center.toInt() + (noteheadHeight / 2)
          val (left, right) = if (shouldStagger) {
            bounds.right - noteheadWidth to bounds.right
          } else {
            (bounds.right - 1.9f * noteheadWidth).toInt() to (bounds.right - 0.9f * noteheadWidth).toInt()
          }
          notehead.setBounds(left, top, right, bottom)
          notehead.alpha = (255 * alphaSource).toInt()
          notehead.draw(this)

          // Draw signs (currently only sharp)
          when (note.sign) {
            Note.Sign.Sharp -> when(previousSignOf(melody, harmony, note, elementPosition)) {
              Note.Sign.Sharp -> null
              else -> sharpPool.borrow().apply { alpha = (255 * alphaSource).toInt() }
            }
            Note.Sign.Flat  -> when(previousSignOf(melody, harmony, note, elementPosition)) {
              Note.Sign.Flat -> null
              else -> flatPool.borrow().apply { alpha = (255 * alphaSource).toInt() }
            }
            Note.Sign.Natural -> when(previousSignOf(melody, harmony, note, elementPosition)) {
              Note.Sign.Natural -> null
              null -> null
              else -> naturalPool.borrow().apply { alpha = (255 * alphaSource).toInt() }
            }
            else            -> null
          }?.let { drawable ->
            val (signLeft, signRight) = if (shouldStagger) {
              (bounds.right - 1.6f * noteheadWidth).toInt() to (bounds.right - 1.1f * noteheadWidth).toInt()
            } else {
              (bounds.right - 2.5f * noteheadWidth).toInt() to (bounds.right - 2.0f * noteheadWidth).toInt()
            }
            val (signTop, signBottom) = when(note.sign) {
              Note.Sign.Flat -> top - 2 * noteheadHeight / 3 to bottom
              else -> top - noteheadHeight / 3 to bottom + noteheadHeight / 3
            }
            drawable.setBounds(signLeft, signTop, signRight, signBottom)
            drawable.alpha = (255 * alphaSource).toInt()
            drawable.draw(this)
          }
          renderLedgerLines(note, left, right)
        }

        // Draw the stem
        if (stemsUp) {
          val stemX = bounds.right - 0.95f * noteheadWidth
          val startY = maxCenter + noteheadHeight * (if (maxWasStaggered) .2f else -.2f)
          val stopY = minCenter - 3 * noteheadHeight
          drawLine(stemX, startY, stemX, stopY, paint)
        } else {
          val stemX = if (hadStaggeredNotes) bounds.right - 0.95f * noteheadWidth
          else bounds.right - 1.85f * noteheadWidth
          val startY = minCenter + noteheadHeight * (if (minWasStaggered) -.2f else .2f)
          val stopY = maxCenter + 3 * noteheadHeight
          drawLine(stemX, startY, stemX, stopY, paint)
        }
      }
    } catch (t: Throwable) {
      warn("Error drawing pressed notes in sequence", t)
      invalidateDrawingLayer()
    }
  }

  fun Canvas.renderLedgerLines(note: Note, left: Int, right: Int) {
    if (!clefs.any { it.covers(note) }) {
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

  fun Canvas.centerOfTone(tone: Int): Float = startPoint - (bottomMostNote + tone - 9.5f) * halfStepWidth

  fun Canvas.renderStaffLines() {
    //val halfStepWidth: Float = axisLength / halfStepsOnScreen
    clefs.flatMap { it.notes }.forEach { note ->
      renderLineAt(
        pointFor(letter = note.letter, octave = note.octave)
      )
    }
  }

  fun Canvas.pointFor(note: Note): Float = pointFor(note.letter, note.octave)

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
  ) {
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
  }


}