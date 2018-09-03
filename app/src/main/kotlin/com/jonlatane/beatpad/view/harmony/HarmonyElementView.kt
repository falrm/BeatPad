package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.MenuItem
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.util.color
import org.jetbrains.anko.*
import android.view.ViewGroup
import android.widget.PopupMenu
import com.jonlatane.beatpad.util.vibrate


class HarmonyElementView @JvmOverloads constructor(
  context: Context,
  var viewModel: HarmonyViewModel? = null
): _RelativeLayout(context) {
  val harmony: Harmony? get() = viewModel?.harmony
  var elementPosition = 0
  val element: Chord? get() = harmony?.changes?.get(elementPosition)
  val chord: Chord? get() = try { harmony?.changeBefore(elementPosition) } catch(e: NoSuchElementException) { null }

  private val editChangeMenu: PopupMenu
  inline val isDownbeat: Boolean get() = harmony?.run {
    elementPosition % subdivisionsPerBeat == 0
  } ?: false

  init {
    isClickable = true
    clipChildren = false
    clipToPadding = false

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

    setOnClickListener { _ ->
      chord?.let {
        viewModel?.paletteViewModel?.orbifold?.chord = it
      }
    }

    setOnLongClickListener { _ ->
      vibrate(150)
      harmony?.let { harmony ->
        val isChange = harmony.isChangeAt(elementPosition)
        viewModel?.selectedChord = chord
        editChangeMenu.menu.findItem(R.id.newChordChange).isVisible = !isChange
        editChangeMenu.menu.findItem(R.id.removeChordChange).isVisible = harmony.changes.values.count { it != null } > 1
        when {
          harmony.isChangeAt(elementPosition) -> {

          }
        }
        editChangeMenu.show()
      }
      true
    }
  }

  val chordText = textView {
    textSize = 20f
    maxLines = 1
    clipChildren = false
    clipToPadding = false
    //ellipsize  = TextUtils.TruncateAt.END
  }.lparams(wrapContent, wrapContent) {
    alignParentLeft()
    setMargins(dip(10), dip(5), dip(-100), dip(5))
  }


  private val paint = Paint()
  private var bounds = Rect()
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.getClipBounds(bounds)
    canvas.drawRhythm()
  }

  override fun invalidate() {
    backgroundColor = chord?.run {
      when {
        isDominant -> color(R.color.dominant)
        isDiminished -> color(R.color.diminished)
        isMinor -> color(R.color.minor)
        isAugmented -> color(R.color.augmented)
        isMajor -> color(R.color.major)
        else -> null
      }
    } ?: color(android.R.color.white)

    var v: ViewGroup = this
    while (v.parent != null && v.parent is ViewGroup) {
      val viewGroup = v.parent as ViewGroup
      viewGroup.clipChildren = false
      viewGroup.clipToPadding = false
      v = viewGroup
    }
    super.invalidate()
  }

  private fun Canvas.drawRhythm() {
    paint.color = 0xAA212121.toInt()
    drawRect(
      bounds.left.toFloat(),
      bounds.top.toFloat(),
      bounds.left.toFloat() + if(isDownbeat) 10f else 1f,
      bounds.bottom.toFloat(),
      paint
    )
  }
}