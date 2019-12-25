package com.jonlatane.beatpad.view.melody.renderer

import BeatClockPaletteConsumer
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import com.github.yamamotoj.cachedproperty.CachedProperty
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.melody.RationalMelody
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
      canvas.drawNotationMelody(
        melody,
        drawAlpha = notationAlpha * alphaMultiplier,
        drawRhythm = false,
        drawColorGuide = false,
        forceDrawColorGuideForCurrentBeat = true,
        forceDrawColorGuideForSelectedBeat = true
      )
    }

    if(isFinalBeat) {
      canvas.drawHorizontalLineInBounds(
        leftSide = false,
        strokeWidth = paint.strokeWidth * 3,
        startY = canvas.pointFor(clefs.flatMap { it.notes }.maxBy { it.heptatonicValue }!!),
        stopY = canvas.pointFor(clefs.flatMap { it.notes }.minBy { it.heptatonicValue }!!)
      )
    }

    paint.color = color(android.R.color.black).withAlpha(
      if (melody != null) (255 * notationAlpha / 3f).toInt()
      else (255 * notationAlpha).toInt()
    )
    var melodiesToRender = sectionMelodiesOfPartType.filter { it != melody }
    if(melody == null) {
      melodiesToRender = melodiesToRender.sortedByDescending { otherMelody ->
        otherMelody.averageTone!!
      }
    }
    val melodyToRenderSelectionAndPlaybackWith = when(melody) {
      null -> melodiesToRender.maxBy { it.subdivisionsPerBeat }
      else -> null
    }
    // Render queue is accessed from two directions; in order from highest to lowest Melody
    val renderQueue = melodiesToRender.toMutableList()
    var index = 0
    while(renderQueue.isNotEmpty()) {
      // Draw highest Melody stems up, lowest stems down, second lowest stems up, second highest
      // down. And repeat.
      val (otherMelody, stemsUp) = when (index % 4) {
        0    -> renderQueue.removeAt(0) to true
        1    -> renderQueue.removeAt(renderQueue.size - 1) to false
        2    -> renderQueue.removeAt(renderQueue.size - 1) to true
        else -> renderQueue.removeAt(0) to false
      }
      val drawSelectionAndPlayback = otherMelody == melodyToRenderSelectionAndPlaybackWith
      canvas.drawNotationMelody(
        otherMelody,
        drawAlpha = melody?.let { notationAlpha / 3f } ?: notationAlpha,
        drawColorGuide = false,
        forceDrawColorGuideForCurrentBeat = drawSelectionAndPlayback,
        forceDrawColorGuideForSelectedBeat = drawSelectionAndPlayback,
        stemsUp = if(melody == null) {
          if (melodiesToRender.size > 2 && renderQueue.isEmpty())
            otherMelody.averageTone!! < melodiesToRender.mapNotNull{ it.averageTone }.average()
          else stemsUp
        } else false
      )
      index++
    }
    flushNotationDrawableCache()
  }

  val Melody<*>.averageTone
    get() = (this as? RationalMelody)?.changes?.values?.flatMap { it.tones }?.average()

  override val harmony: Harmony get() = section.harmony
  val meter: Harmony.Meter get() = harmony.meter
  val isFinalBeat: Boolean get() = (beatPosition + 1) % meter.defaultBeatsPerMeasure == 0

  fun flushNotationDrawableCache() {
    listOf(filledNoteheadPool, sharpPool, xNoteheadPool)
      .forEach { it.flushNotationDrawableCache() }
  }

  val sharpPool: DrawablePool
  val flatPool: DrawablePool
  val doubleSharpPool: DrawablePool
  val doubleFlatPool: DrawablePool
  val naturalPool: DrawablePool
  val filledNoteheadPool: DrawablePool
  val xNoteheadPool: DrawablePool

  class DrawablePool(val drawableResource: Int, val context: Context) : DefaultPool<Drawable>(256) {
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
    get() = section.melodies
      .filter { !it.isDisabled }
      .map { it.melody }

  val renderedMelodiesCache: CachedProperty<List<Melody<*>>>
  val renderedMelodies: List<Melody<*>>
  get() = when(BeatClockPaletteConsumer.playbackMode) {
    BeatClockPaletteConsumer.PlaybackMode.SECTION -> sectionMelodies
    BeatClockPaletteConsumer.PlaybackMode.PALETTE -> palette.sections.flatMap {
      it.melodies.filter { !it.isDisabled }
    }.map { it.melody }
  }

  val sectionMelodiesOfPartTypeCache: CachedProperty<List<Melody<*>>>
  val sectionMelodiesOfPartType: List<Melody<*>>
    get() = when(viewType) {
      BaseMelodyBeatRenderer.ViewType.DrumPart -> {
        sectionMelodies.filter { it.drumPart }
      }
      BaseMelodyBeatRenderer.ViewType.OtherNonDrumParts ->
        arrayOf(melody).filterNotNull() +
        sectionMelodies.filter { !it.drumPart }
      else -> emptyList()
    }

  fun Canvas.drawNotationMelody(
    melody: Melody<*>,
    drawAlpha: Float = notationAlpha,
    drawRhythm: Boolean = false,
    drawColorGuide: Boolean = true,
    forceDrawColorGuideForCurrentBeat: Boolean = false,
    forceDrawColorGuideForSelectedBeat: Boolean = false,
    stemsUp: Boolean = true
  ) {
    val maxSubdivisonsPerBeatUnder7 = (renderedMelodies + melody)
      .filter { it.subdivisionsPerBeat <= 7 }
      .map { it.subdivisionsPerBeat }.max() ?: 7
    val maxSubdivisonsPerBeatUnder13 = (renderedMelodies + melody)
      .filter { it.subdivisionsPerBeat <= 13 }
      .map { it.subdivisionsPerBeat }.max() ?: 13
    val maxSubdivisonsPerBeat = (renderedMelodies + melody)
      .map { it.subdivisionsPerBeat }.max() ?: 24
    val maxBoundsWidthUnder7 = min(
      (overallBounds.right - overallBounds.left) / maxSubdivisonsPerBeatUnder7,
      round(letterStepSize * 10).toInt()
    )
    val maxBoundsWidthUnder13 = min(
      (overallBounds.right - overallBounds.left) / maxSubdivisonsPerBeatUnder13,
      round(letterStepSize * 10).toInt()
    )
    val maxBoundsWidth = min(
      (overallBounds.right - overallBounds.left) / maxSubdivisonsPerBeat,
      round(letterStepSize * 10).toInt()
    )
    iterateSubdivisions(melody) { elementPosition ->
      bounds.right = bounds.left + when {
        melody.subdivisionsPerBeat <= 7 -> maxBoundsWidthUnder7
        melody.subdivisionsPerBeat <= 13 -> maxBoundsWidthUnder13
        else -> maxBoundsWidth
      }
      colorGuideAlpha = (when {
        !drawColorGuide                -> when {
          forceDrawColorGuideForCurrentBeat && isCurrentlyPlayingBeat -> 119
          forceDrawColorGuideForSelectedBeat && isSelectedBeatInHarmony -> 69
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

  data class PlaybackNoteRequest(
    val tones: Set<Int>,
    val melody: UUID,
    val chord: Chord
  )
  val playbackNoteCache: LruCache<PlaybackNoteRequest, List<Note>>
  fun getPlaybackNotes(
    tones: Set<Int>,
    melody: Melody<*>,
    chord: Chord
  ): List<Note> {
    val request = PlaybackNoteRequest(tones, melody.id, chord)
    return when(val cacheResult = playbackNoteCache.get(request)) {
      null -> {
        computePlaybackNotes(tones, melody, chord).also {
          playbackNoteCache.put(request, it)
        }
      }
      else -> cacheResult
    }
  }

  fun computePlaybackNotes(
    tones: Set<Int>,
    melody: Melody<*>,
    chord: Chord
  ): List<Note> = tones.map {
    val playbackTone = melody.playbackToneUnder(it, chord)
    Note.nameNoteUnderChord(playbackTone, chord)
  }

  data class PreviousSignRequest(
    val melody: UUID,
    val harmony: UUID,
    val note: Note,
    val melodyPosition: Int
  )
  val previousSignCache: LruCache<PreviousSignRequest, Note.Sign>
  fun previousSignOf(
    melody: Melody<*>,
    harmony: Harmony,
    note: Note,
    melodyPosition: Int
  ): Note.Sign {
    val request = PreviousSignRequest(melody.id, harmony.id, note, melodyPosition)
    return when(val cacheResult = previousSignCache.get(request)) {
      null -> {
        calculatePreviousSignOf(melody, harmony, note, melodyPosition).also {
          previousSignCache.put(request, it)
        }
      }
      else -> cacheResult
    }
  }
  fun calculatePreviousSignOf(
    melody: Melody<*>,
    harmony: Harmony,
    note: Note,
    melodyPosition: Int
  ): Note.Sign {
    val currentBeatPosition = melodyPosition.toFloat() / melody.subdivisionsPerBeat
    val lastDownbeat = (0..Int.MAX_VALUE).first {
      it % harmony.meter.defaultBeatsPerMeasure == 0 &&
      it <= currentBeatPosition && it + harmony.meter.defaultBeatsPerMeasure > currentBeatPosition
    }
    var result: Note.Sign = Note.Sign.None
    melody.changes.navigableKeySet()
      .subSet(lastDownbeat * melody.subdivisionsPerBeat, true, melodyPosition, true)
      .reversed()
      .any { changeIndex ->
        val chordAtTheTime = chordAt(changeIndex, melody)
        val change = melody.changes[changeIndex]
        val tones: Set<Int> = (change.let {
          (it as? RationalMelody.Element)?.tones
        } ?: emptySet<Int>()).filter {
          !(changeIndex == melodyPosition && note.tone == melody.playbackToneUnder(it, chordAtTheTime))
        }.toSet()

        val playbackNotes = tones.map {
          val playbackTone = melody.playbackToneUnder(it, chordAtTheTime)
          Note.nameNoteUnderChord(playbackTone, chordAtTheTime)
        }
        val matchingNotes = playbackNotes.filter {
          it.heptatonicValue == note.heptatonicValue && it.octave == note.octave
        }

        if(matchingNotes.toSet().size == 1) {
           result = matchingNotes.first().sign
        }
        matchingNotes.isNotEmpty()
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
    try {
      val change = melody.changeBefore(elementPosition % melody.length)
      val tones: Set<Int> = change.let {
        (it as? RationalMelody.Element)?.tones
      } ?: emptySet()

      if (tones.isNotEmpty()) {
        val boundsWidth = bounds.width()
        val maxFittableNoteheadWidth = ceil(boundsWidth / 2.6f).toInt()
        val noteheadWidth = min((letterStepSize * 2).toInt(), maxFittableNoteheadWidth)
        val noteheadHeight = noteheadWidth//(bounds.right - bounds.left)

        val playbackNotes = getPlaybackNotes(tones, melody, chord)
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
          val previousSign = previousSignOf(melody, harmony, note, elementPosition)
          when (note.sign) {
            Note.Sign.Sharp -> when(previousSign) {
              Note.Sign.Sharp -> null
              else -> sharpPool
            }
            Note.Sign.Flat  -> when(previousSign) {
              Note.Sign.Flat -> null
              else -> flatPool
            }
            Note.Sign.DoubleSharp -> when(previousSign) {
              Note.Sign.DoubleSharp -> null
              else -> doubleSharpPool
            }
            Note.Sign.DoubleFlat -> when(previousSign) {
              Note.Sign.DoubleFlat -> null
              else -> doubleFlatPool
            }
            Note.Sign.Natural, Note.Sign.None -> when(previousSign) {
              Note.Sign.Natural, Note.Sign.None -> null
              else -> naturalPool
            }
          }?.borrow()
            ?.apply { alpha = (255 * alphaSource).toInt() }
            ?.let { drawable ->
              val (signLeft, signRight) = if (shouldStagger) {
                (bounds.right - 1.6f * noteheadWidth).toInt() to (bounds.right - 1.1f * noteheadWidth).toInt()
              } else {
                (bounds.right - 2.5f * noteheadWidth).toInt() to (bounds.right - 2.0f * noteheadWidth).toInt()
              }
              val (signTop, signBottom) = when (note.sign) {
                Note.Sign.Flat, Note.Sign.DoubleFlat ->
                  top - 2 * noteheadHeight / 3 to bottom
                Note.Sign.DoubleSharp ->
                  top + noteheadHeight / 4 to bottom - noteheadHeight / 4
                Note.Sign.Sharp, Note.Sign.Natural, Note.Sign.None ->
                  top - noteheadHeight / 3 to bottom + noteheadHeight / 3
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
          val startY = minCenter + noteheadHeight * when {
            minWasStaggered || hadStaggeredNotes -> -.2f
            else -> .2f
          }
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