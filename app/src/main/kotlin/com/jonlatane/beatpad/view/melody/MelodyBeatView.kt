package com.jonlatane.beatpad.view.melody

import BeatClockPaletteConsumer
import BeatClockPaletteConsumer.ticksPerBeat
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.util.LruCache
import android.util.SparseArray
import android.view.MotionEvent
import com.github.yamamotoj.cachedproperty.CachedProperty
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView
import com.jonlatane.beatpad.view.melody.input.MelodyBeatEventArticulationHandler
import com.jonlatane.beatpad.view.melody.input.MelodyBeatEventEditingHandler
import com.jonlatane.beatpad.view.melody.renderer.MelodyBeatRenderer
import com.jonlatane.beatpad.view.melody.renderer.BaseMelodyBeatRenderer.ViewType
import com.jonlatane.beatpad.view.melody.renderer.MelodyBeatNotationRenderer
import com.jonlatane.beatpad.view.melody.renderer.MelodyBeatNotationRenderer.*
import com.jonlatane.beatpad.view.melody.renderer.Note
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingModifiers
import com.jonlatane.beatpad.view.palette.BeatScratchToolbar
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

/**
 * BeatViews
 */
@SuppressLint("ViewConstructor")
class MelodyBeatView constructor(
  context: Context,
  override val viewModel: MelodyViewModel
) : BaseColorboardView(context), MelodyBeatRenderer,
  MelodyBeatEventArticulationHandler, MelodyBeatEventEditingHandler, AnkoLogger {
  override var viewType: ViewType = ViewType.OtherNonDrumParts
  override val palette get() = viewModel.paletteViewModel.palette
  override var section : Section = Section()
    private set
  override val melody: Melody<*>? get() = viewModel.openedMelody?.let {
    when {
      viewType == ViewType.DrumPart && it.drumPart -> it
      viewType != ViewType.DrumPart && !it.drumPart -> it
      else -> null
    }
  }
  override var isCurrentlyPlayingBeat = false
  override var isSelectedBeatInHarmony = false
  override val displayType: MelodyViewModel.DisplayType get() = viewModel.displayType
  override val renderableToneBounds: Rect = Rect()
  override val colorblockAlpha: Float get() = viewModel.beatAdapter.colorblockAlpha
  override val notationAlpha: Float get() = viewModel.beatAdapter.notationAlpha
  override val filledNoteheadPool = LocalizedDrawablePool(FILLED_NOTEHEAD_POOL)
  override val sharpPool = LocalizedDrawablePool(SHARP_POOL)
  override val flatPool = LocalizedDrawablePool(FLAT_POOL)
  override val doubleSharpPool = LocalizedDrawablePool(DOUBLE_SHARP_POOL)
  override val doubleFlatPool = LocalizedDrawablePool(DOUBLE_FLAT_POOL)
  override val naturalPool = LocalizedDrawablePool(NATURAL_POOL)
  override val xNoteheadPool = LocalizedDrawablePool(X_NOTEHEAD_POOL)
  override val sectionMelodiesOfPartTypeCache = CachedProperty { super.sectionMelodiesOfPartType }
  override val sectionMelodiesOfPartType: List<Melody<*>> by sectionMelodiesOfPartTypeCache
  override val renderedMelodiesCache = CachedProperty { super.renderedMelodies }
  override val renderedMelodies: List<Melody<*>> by renderedMelodiesCache
  override val previousSignCache: LruCache<PreviousSignRequest, Note.Sign> = PREVIOUS_SIGN_CACHE
  override val playbackNoteCache: LruCache<PlaybackNoteRequest, List<Note>> = PLAYBACK_NOTE_CACHE

  init {
    showSteps = true
  }

  override var sectionStartBeatPosition: Int = 0
  override var beatPosition = 0
  set(value) {
    val (beatPos, section) = when(viewModel.paletteViewModel.interactionMode) {
      BeatScratchToolbar.InteractionMode.VIEW -> {
        //TODO LAZY LAZY assuming 4/4 here
        var sectionStartBeat = 0
        var section = viewModel.paletteViewModel.palette.sections.first()
        for(it in viewModel.paletteViewModel.palette.sections) {
          val sectionLength = section.harmony.run { length / subdivisionsPerBeat}
          if(sectionStartBeat + sectionLength <= value) {
            sectionStartBeat += sectionLength
          } else {
            section = it
            break
          }
        }
        value - sectionStartBeat to section
      }
      else                                    -> {
        value to BeatClockPaletteConsumer.section!!
      }
    }
    field = beatPos
    this.section = section
  }

  override val downPointers = SparseArray<PointF>()
  //override var initialHeight: Int? = null
  override val renderVertically = true
  override val halfStepsOnScreen = 88f

  inline val elementRange: IntRange? get() = melody?.let { elementRangeFor(it) }
  val drawWidth get() = elementRange?.let { (width.toFloat() / it.size).toInt() } ?: 0
  override val drawPadding: Int
    get() = if (drawWidth > dip(27f)) dip(5)
    else Math.max(0, drawWidth - dip(22f))
  override val nonRootPadding get() = drawPadding

  override val overallBounds = Rect()
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    try {
      canvas.getClipBounds(overallBounds)
      renderMelodyBeat(canvas)
    } catch(t: Throwable) {
      error("Failed to render MelodyBeatView", t)
    }
  }


  override fun melodyOffsetAt(elementPosition: Int): Int
    = chordAt(elementPosition, viewModel.openedMelody!!).let { chord ->
    info("Computing edit under $chord")
      viewModel.let { it.openedMelody?.offsetUnder(chord) } ?: 0
    }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return when (viewModel.melodyEditingModifiers.modifier) {
      MelodyEditingModifiers.Modifier.None         -> false
      MelodyEditingModifiers.Modifier.Editing      -> onTouchEditEvent(event)
      MelodyEditingModifiers.Modifier.Articulating -> true//onTouchArticulateEvent(event)
      MelodyEditingModifiers.Modifier.Transposing  -> true
      else                                         -> false
    }
  }

  override fun getPositionAndElement(x: Float): Pair<Int, Transposable<*>?>? {
    return melody?.let { melody ->
      val elementRange: IntRange = elementRange!!
      val elementIndex: Int = (elementRange.size * x / width).toInt()
      val elementPosition = beatPosition * melody.subdivisionsPerBeat + elementIndex
      return elementPosition to melody.changes[elementPosition % melody.length]
    }
  }

  override fun getTone(y: Float): Int {
    return Math.round(lowestPitch + 88 * (height - y) / height)
  }


  override fun updateMelodyDisplay() = viewModel.updateMelodyDisplay()

  override fun invalidateDrawingLayer() {
    if(viewType != ViewType.Unused) {
      invalidate()
    }
  }

  fun invalidateDrawingLayerIfPositionChanged(
    oldTick: Int,
    newTick: Int
  ) {
    val positionsPerBeat = sectionMelodiesOfPartType
      .maxBy { it.subdivisionsPerBeat }?.subdivisionsPerBeat ?: 1
    val (oldPosition, newPosition) = arrayOf(oldTick, newTick).map {
      it.convertPatternIndex(
        fromSubdivisionsPerBeat = ticksPerBeat,
        toSubdivisionsPerBeat = positionsPerBeat
      )
    }
    when {
      viewType == ViewType.Unused -> {}
      oldPosition != newPosition -> invalidate()
    }
  }

  companion object {
    val DEFAULT_CHORD = Chord(0, intArrayOf(0,1,2,3,4,5,6,7,8,9,10,11))
    val PREVIOUS_SIGN_CACHE: LruCache<PreviousSignRequest, Note.Sign> = LruCache(10240)
    val PLAYBACK_NOTE_CACHE: LruCache<PlaybackNoteRequest, List<Note>> = LruCache(10240)
    val FILLED_NOTEHEAD_POOL = DrawablePool(R.drawable.notehead_filled, MainApplication.instance)
    val SHARP_POOL = DrawablePool(R.drawable.sharp, MainApplication.instance)
    val FLAT_POOL = DrawablePool(R.drawable.flat, MainApplication.instance)
    val DOUBLE_SHARP_POOL = DrawablePool(R.drawable.doublesharp, MainApplication.instance)
    val DOUBLE_FLAT_POOL = DrawablePool(R.drawable.doubleflat, MainApplication.instance)
    val NATURAL_POOL = DrawablePool(R.drawable.natural_sign, MainApplication.instance)
    val X_NOTEHEAD_POOL: DrawablePool = DrawablePool(R.drawable.notehead_x, MainApplication.instance)
  }
}
