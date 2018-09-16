package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import android.widget.PopupMenu
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.service.convertPatternIndex
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.size
import com.jonlatane.beatpad.util.vibrate
import org.jetbrains.anko.toast
import org.jetbrains.anko.withAlpha


class HarmonyElementView @JvmOverloads constructor(
  context: Context,
  var viewModel: HarmonyViewModel? = null
): View(context) {
  val harmony: Harmony? get() = viewModel?.harmony

  var beatPosition = 0

  inline val elementRange: IntRange? get() = harmony?.let { harmony ->
    (beatPosition * harmony.subdivisionsPerBeat) until
      Math.min((beatPosition + 1) * harmony.subdivisionsPerBeat, harmony.length)
  }

  val downPointers = SparseArray<PointF>()
  //val element: Chord? get() = harmony?.changes?.get(elementPosition)
  //val chord: Chord? get() = try { harmony?.changeBefore(elementPosition) } catch(e: NoSuchElementException) { null }

  private val editChangeMenu: PopupMenu

  init {
    isClickable = true

    editChangeMenu = PopupMenu(context, this)
    editChangeMenu.inflate(R.menu.harmony_element_menu)
    editChangeMenu.setOnMenuItemClickListener { item ->
      when (item.itemId) {
      //R.id.newDrawnPattern -> adapter.newToneSequence()
        R.id.newChordChange -> {context.toast("TODO")}
        R.id.editChordChange -> {context.toast("TODO")}
        R.id.removeChordChange -> {context.toast("TODO")}
        else -> context.toast("TODO!")
      }
      true
    }

    setOnClickListener { event ->
      val chord = getPositionAndElement(event.x)?.second
      chord?.let {
        viewModel?.paletteViewModel?.orbifold?.chord = it
      }
    }

    setOnLongClickListener { _ ->
      vibrate(150)
      harmony?.let { harmony ->
        /*val isChange = harmony.isChangeAt(elementPosition)
        viewModel?.selectedChord = chord
        editChangeMenu.menu.findItem(R.id.newChordChange).isVisible = !isChange
        editChangeMenu.menu.findItem(R.id.removeChordChange).isVisible = harmony.changes.values.count { it != null } > 1
        when {
          harmony.isChangeAt(elementPosition) -> {

          }
        }*/
        editChangeMenu.show()
      }
      true
    }
  }


  private val paint = Paint()

  private val overallBounds = Rect()
  private var bounds = Rect()
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
        /*chord = harmony?.let { harmony ->
          val harmonyPosition = elementPosition.convertPatternIndex(melody, harmony)
          harmony.changeBefore(harmonyPosition)
        } ?: viewModel?.orbifold?.chord ?: MelodyBeatView.DEFAULT_CHORD
        colorGuideAlpha = if (
          viewModel?.playbackTick?.convertPatternIndex(
            from = BeatClockPaletteConsumer.ticksPerBeat,
            to = melody
          ) == elementPosition
        ) 255 else 187
        canvas.drawColorGuide()
        canvas.drawStepNotes(melody, elementPosition)*/
        val chord = harmony.changeBefore(elementPosition)
        paint.color = chord.run {
          when {
            isDominant -> color(R.color.dominant)
            isDiminished -> color(R.color.diminished)
            isMinor -> color(R.color.minor)
            isAugmented -> color(R.color.augmented)
            isMajor -> color(R.color.major)
            else -> color(android.R.color.white)
          }
        }

        val paintAlpha = if (
          viewModel?.paletteViewModel?.playbackTick?.convertPatternIndex(
            from = BeatClockPaletteConsumer.ticksPerBeat,
            to = harmony
          ) == elementPosition
        ) 255 else 187
        paint.color = paint.color.withAlpha(paintAlpha)
        canvas.drawRect(
          bounds.left.toFloat(),
          bounds.top.toFloat(),
          bounds.right.toFloat(),
          bounds.bottom.toFloat(),
          paint
        )
        canvas.drawRhythm(elementIndex)
      }
    }
  }

  fun getPositionAndElement(x: Float): Pair<Int, Chord?>? {
    return harmony?.let { harmony ->
      val elementRange: IntRange = elementRange!!
      val elementIndex: Int = (elementRange.size * x / width).toInt()
      val elementPosition = Math.min(beatPosition * harmony.subdivisionsPerBeat + elementIndex, harmony.length - 1)
      return elementIndex to harmony.changes[elementPosition]
    }
  }

  private fun Canvas.drawRhythm(elementIndex: Int) {
    paint.color = 0xAA212121.toInt()
    drawRect(
      bounds.left.toFloat(),
      bounds.top.toFloat(),
      bounds.left.toFloat() + if (elementIndex == 0) 10f else 5f,
      bounds.bottom.toFloat(),
      paint
    )
  }
}