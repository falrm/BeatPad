package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.zoomableRecyclerView
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.*

class HarmonyView(
  context: Context,
  val viewModel: PaletteViewModel,
  init: HideableRelativeLayout.() -> Unit = {}
) : HideableRelativeLayout(context), AnkoLogger {
  // We will render these at the top level on scroll
  val chordChangeLabels: MutableMap<Int, TextView> = mutableMapOf()
  private val chordChangeLabelPool: DefaultPool<TextView> = object : DefaultPool<TextView>(16) {
    override fun produceInstance() = textView {
      textSize = 20f
      maxLines = 1
      typeface = MainApplication.chordTypeface
      elevation = 5f
    }.lparams(width = wrapContent, height = wrapContent) {
      topMargin = dip(10)
      marginStart = dip(40)
      alignParentLeft()
      alignParentTop()
    }
    override fun clearInstance(instance: TextView): TextView = instance.apply {
      info("Clearing textview $text")
      text = ""
      translationX = 0f
    }
  }

  init {
    viewModel.harmonyView = this
    backgroundColor = color(R.color.colorPrimaryDark)
    viewModel.harmonyViewModel.harmonyElementRecycler = zoomableRecyclerView {
      id = R.id.center_h_scroller
      isFocusableInTouchMode = true
      elevation = 3f
      addOnScrollListener(object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
          super.onScrolled(recyclerView, dx, dy)
          syncScrollingChordText()
        }
      })
    }.lparams {
      width = ViewGroup.LayoutParams.MATCH_PARENT
      height = ViewGroup.LayoutParams.WRAP_CONTENT
      topMargin = dip(5)
      marginStart = dip(30)
      alignParentRight()
      alignParentTop()
    }.apply {
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
        isItemPrefetchEnabled = false
      }


      zoomHandler = { xDelta, yDelta ->
        AnkoLogger<MelodyViewModel>().info("Zooming: xDelta=$xDelta, yDelta=$yDelta")
        when {
          (xDelta.toInt() != 0) -> {
            viewModel.harmonyViewModel.chordAdapter?.apply {
              elementWidth += xDelta.toInt()
              notifyDataSetChanged()
            }
            true
          }
          else -> false
        }
      }
      overScrollMode = View.OVER_SCROLL_NEVER
      viewModel.harmonyViewModel.chordAdapter = HarmonyChordAdapter(viewModel, this)
      adapter = viewModel.harmonyViewModel.chordAdapter
      adapter.registerAdapterDataObserver(
        object : RecyclerView.AdapterDataObserver() {
          override fun onItemRangeInserted(start: Int, count: Int) {
            //updateEmptyViewVisibility(this@recyclerView)
          }

          override fun onItemRangeRemoved(start: Int, count: Int) {
            //updateEmptyViewVisibility(this@recyclerView)
          }
        })
    }
    init()
  }

  fun syncScrollingChordText() {
    val (firstItemPosition, lastItemPosition) = (viewModel.harmonyViewModel
      .harmonyElementRecycler?.layoutManager as? LinearLayoutManager)?.run {
      findFirstVisibleItemPosition() to findLastVisibleItemPosition()
    } ?: 0 to 0
    val visibleChanges = viewModel.harmonyViewModel.harmony?.let { harmony ->
      harmony.changes
        .headMap(lastItemPosition)
        .tailMap(Math.min(firstItemPosition, harmony.lowerKey(firstItemPosition)))
    }
    info("Visible changes: $visibleChanges")
    val horizontalScrollOffset =  viewModel.harmonyViewModel
      .harmonyElementRecycler?.computeHorizontalScrollOffset()?.let { it % viewModel.harmonyViewModel.chordAdapter!!.elementWidth } ?: 0

    visibleChanges?.forEach { position, chord ->
      val label = chordChangeLabels[position]
        ?: chordChangeLabelPool.borrow().also { chordChangeLabels[position] = it }
      label.apply {
        if(text == "") text = chord.name
        val translationX = Math.max(
          0,
          (
            ((position - firstItemPosition) * viewModel.harmonyViewModel.chordAdapter!!.elementWidth)
              - horizontalScrollOffset
            )
        ).toFloat()
        info("Setting translationX of $text to $translationX")
        this.translationX = translationX
      }
    }

    visibleChanges?.let { changes ->
      val entriesToRemove = chordChangeLabels.filterKeys { !changes.containsKey(it) }
      entriesToRemove.forEach { key, textView ->
        chordChangeLabels.remove(key)
        chordChangeLabelPool.recycle(textView)
      }
    }
  }
}
