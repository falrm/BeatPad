package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.util.vibrate
import org.jetbrains.anko.toast
import org.jetbrains.anko.withAlpha


@SuppressLint("ViewConstructor")
class HarmonyBeatView constructor(
  context: Context,
  var viewModel: HarmonyViewModel
): View(context), Storage, Patterns {
  override val storageContext: Context get() = context
  val harmony: Harmony? get() = viewModel.harmony
  val section: Section? get() = viewModel.section

  var beatPosition = 0
  internal var beatSelectionAnimationPosition: Int = 0

  inline val elementRange: IntRange? get() = harmony?.let { harmony ->
    (beatPosition * harmony.subdivisionsPerBeat) until
      Math.min((beatPosition + 1) * harmony.subdivisionsPerBeat, harmony.length)
  }

  val downPointers = SparseArray<PointF>()
  //val element: Chord? get() = harmony?.changes?.get(elementPosition)
  //val chord: Chord? get() = try { harmony?.changeBefore(elementPosition) } catch(e: NoSuchElementException) { null }

  private val editChangeMenu: PopupMenu
  private val lastTouchDownXY = FloatArray(2)
  private val lastTouchDownX get() = lastTouchDownXY[0]
  private val lastTouchDownY get() = lastTouchDownXY[1]
  private val removeChordMenuItem get() = editChangeMenu.menu.findItem(R.id.removeChordChange)
  private val pasteHarmonyMenuItem get() = editChangeMenu.menu.findItem(R.id.pasteHarmony)

  init {
    isClickable = true

    editChangeMenu = PopupMenu(context, this)
    editChangeMenu.inflate(R.menu.harmony_element_menu)
    editChangeMenu.setOnDismissListener {
      viewModel.apply {
        if(!isChoosingHarmonyChord) selectedHarmonyElements = null
      }
    }
    editChangeMenu.setOnMenuItemClickListener { item ->
      viewModel.isChoosingHarmonyChord = false
      when (item.itemId) {
        R.id.newChordChange -> {
          val position = viewModel.selectedHarmonyElements!!.first
          harmony!!.changes[position] = harmony!!.changeBefore(position)
          viewModel.harmonyView?.syncScrollingChordText()
          editSelectedChord()
        }
        R.id.editChordChange -> {
          editSelectedChord()
        }
        R.id.removeChordChange -> {
          val position = viewModel.selectedHarmonyElements!!.first
          val key = harmony!!.floorKey(position)!!
          harmony!!.changes.remove(key)
          viewModel.selectedHarmonyElements = null
          viewModel.harmonyView?.syncScrollingChordText()
        }
        R.id.copyHarmony -> {
          val text = harmony?.toURI()?.toString() ?: ""
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          val clip = ClipData.newPlainText("BeatScratch Harmony", text)
          clipboard.setPrimaryClip(clip)
          //clipboard.primaryClip = clip
          context.toast("Copied BeatScratch Harmony data to clipboard!")
        }
        R.id.pasteHarmony -> viewModel.pasteHarmony()
        else -> context.toast("TODO!")
      }
      true
    }

    setOnClickListener {
      val (position, chord) = getPositionAndElement(lastTouchDownXY[0]) ?: null to null
      chord?.let {
        viewModel.paletteViewModel?.orbifold?.disableNextTransitionAnimation()
        viewModel.paletteViewModel?.orbifold?.chord = it
      }
      position?.let {
        harmony?.let { harmony ->
          val newPlaybackTick = position.convertPatternIndex(
            fromSubdivisionsPerBeat = harmony.subdivisionsPerBeat,
            toSubdivisionsPerBeat = 24,
            toLength = Math.floor(harmony.length.toDouble() / harmony.subdivisionsPerBeat)
              .toInt() * 24
          )
          BeatClockPaletteConsumer.tickPosition = newPlaybackTick
          viewModel.paletteViewModel?.playbackTick = newPlaybackTick
        }
      }
    }

    setOnTouchListener { _, event ->
      // save the X,Y coordinates
      if (event.actionMasked == MotionEvent.ACTION_DOWN) {
        lastTouchDownXY[0] = event.x
        lastTouchDownXY[1] = event.y
      }

      // let the touch event pass on to whoever needs it
      false
    }

    setOnLongClickListener {
      vibrate(150)
      harmony?.let { harmony ->
        getPositionAndElement(lastTouchDownX)?.let { (position, _) ->
          viewModel.selectedHarmonyElements = position..position
        }
        removeChordMenuItem.isEnabled = harmony.changes.count() > 1
        pasteHarmonyMenuItem.isEnabled = viewModel.getClipboardHarmony() != null
        editChangeMenu.show()
      }
      true
    }
  }


  private val paint = Paint()

  private val overallBounds = Rect()
  private val bounds = Rect()
  private val hsv = FloatArray(3)
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.getClipBounds(overallBounds)
    val overallWidth = overallBounds.right - overallBounds.left
    bounds.apply {
      top = overallBounds.top
      bottom = overallBounds.bottom
      left = overallBounds.left
      right = overallBounds.right
    }


    paint.color = 0xFFFFFFFF.toInt()
    canvas.drawRect(
      bounds.left.toFloat(),
      bounds.top.toFloat(),
      bounds.right.toFloat(),
      bounds.bottom.toFloat(),
      paint
    )

    harmony?.let { harmony ->
      val elementRange: IntRange = elementRange!! /*(beatPosition * melody.subdivisionsPerBeat) until
          Math.min((beatPosition + 1) * melody.subdivisionsPerBeat, melody.length - 1)*/
      val elementCount = elementRange.size
      for((elementIndex, elementPosition) in elementRange.withIndex()) {
        bounds.apply {
          left = (overallWidth.toFloat() * elementIndex / elementCount).toInt()
          right = (overallWidth.toFloat() * (elementIndex+1) / elementCount).toInt()
        }
        val isPlaying = viewModel?.paletteViewModel?.playbackTick?.convertPatternIndex(
          from = BeatClockPaletteConsumer.ticksPerBeat,
          to = harmony
        ) == elementPosition
        val isSelected = viewModel?.selectedHarmonyElements?.contains(elementPosition) ?: false
        val isFaded = !isSelected && viewModel?.selectedHarmonyElements != null
        fun Int.withHighlight() = this.withAlpha(
          when {
            isPlaying -> 255
            isSelected -> when((beatSelectionAnimationPosition + (elementPosition * SelectedChordAnimation.steps).mod12).mod12) {
              0, 3, 5, 7, 11 -> 127
              else -> 255
            }
            isFaded -> 41
            else -> 187
          }
        ).let {
          when {
            isPlaying -> {
              Color.colorToHSV(it, hsv)
//                hsv[1] = 0.1f
              hsv[2] = 0.5f
              Color.HSVToColor(hsv)
            }
            else          -> it
          }
        }

        val chord = harmony.changeBefore(elementPosition)
        paint.color = chord.run {
          when {
            isDominant -> color(R.color.dominant).withHighlight()
            isDiminished -> color(R.color.diminished).withHighlight()
            isMinor -> color(R.color.minor).withHighlight()
            isAugmented -> color(R.color.augmented).withHighlight()
            isMajor -> color(R.color.major).withHighlight()
            // Tint the white beat - inverse
            else -> color(R.color.colorPrimaryDark).withAlpha(
              when {
                isPlaying -> 100
                isSelected -> when((beatSelectionAnimationPosition + (elementPosition * SelectedChordAnimation.steps).mod12).mod12) {
                  0, 3, 5, 7, 11 -> 67
                  else -> 23
                }
                isFaded -> 128
                else -> 0
              }
            )
          }
        }

        canvas.drawRect(
          bounds.left.toFloat(),
          bounds.top.toFloat(),
          bounds.right.toFloat(),
          bounds.bottom.toFloat(),
          paint
        )
        canvas.drawRhythm(harmony, elementIndex)
      }
    }
    if(harmony == null) {
      canvas.drawRhythm(null, 0)
    }
    bounds.apply {
      left = overallBounds.right
      right = overallBounds.right
    }
    canvas.drawRhythm(harmony, harmony?.subdivisionsPerBeat ?: 1)
  }

  fun getPositionAndElement(x: Float): Pair<Int, Chord?>? {
    return harmony?.let { harmony ->
      val elementRange: IntRange = elementRange!!
      val elementIndex: Int = (elementRange.size * x / width).toInt()
      val elementPosition = Math.min(beatPosition * harmony.subdivisionsPerBeat + elementIndex, harmony.length - 1)
      return elementPosition to harmony.changeBefore(elementPosition)
    }
  }

  private fun Canvas.drawRhythm(harmony: Harmony?, elementIndex: Int) {
    paint.color = 0xAA212121.toInt()
    val halfWidth = if (elementIndex % (harmony?.subdivisionsPerBeat ?: 1) == 0) 5f else 1f
    drawRect(
      bounds.left.toFloat() - halfWidth,
      bounds.top.toFloat(),
      bounds.left.toFloat() + halfWidth,
      bounds.bottom.toFloat(),
      paint
    )
  }

  private fun editSelectedChord() {
    var chord: Chord? = null
    val chordRange = viewModel.selectedHarmonyElements?.let {
      chord = harmony!!.changeBefore(it.first)
      val start = harmony!!.floorKey(it.first)
      var end = harmony!!.higherKey(it.first) - 1
      if(end < start) end = harmony!!.length - 1
      start..end
    }
    viewModel.paletteViewModel?.orbifold?.let {
      it.disableNextTransitionAnimation()
      it.chord = chord!!
    }
    viewModel.selectedHarmonyElements = chordRange
    viewModel.isChoosingHarmonyChord = true
  }
}