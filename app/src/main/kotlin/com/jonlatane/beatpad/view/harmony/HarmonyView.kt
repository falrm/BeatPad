package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
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
  override val viewModel: PaletteViewModel,
  init: HideableRelativeLayout.() -> Unit = {}
) : HideableRelativeLayout(context), ChordTextPositioner, AnkoLogger {
  override val marginForKey = dip(5)

  // We will render these at the top level on scroll
  override val chordChangeLabels: MutableMap<Int, TextView> = mutableMapOf()
  override val chordChangeLabelPool: DefaultPool<TextView> = defaultChordChangeLabelPool
  override val recycler: RecyclerView get() = viewModel.harmonyViewModel.harmonyElementRecycler!!

  init {
    viewModel.harmonyView = this
    backgroundColor = color(R.color.colorPrimaryDark)
    val textView1 = chordChangeLabelPool.borrow().apply {
      id = View.generateViewId()
    }
    viewModel.harmonyViewModel.harmonyElementRecycler = zoomableRecyclerView {
      id = R.id.center_h_scroller
      isFocusableInTouchMode = true
      elevation = 3f
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
        isItemPrefetchEnabled = false
      }
      overScrollMode = View.OVER_SCROLL_NEVER


      zoomHandler = { xDelta, yDelta ->
        AnkoLogger<MelodyViewModel>().info("Zooming: xDelta=$xDelta, yDelta=$yDelta")
        when {
          (xDelta.toInt() != 0) -> {
            viewModel.harmonyViewModel.beatAdapter.apply {
              elementWidth += xDelta.toInt()
              notifyDataSetChanged()
            }
            true
          }
          else -> false
        }
      }

      zoomFinishedHandler = { viewModel.melodyViewModel.onZoomFinished() }
      overScrollMode = View.OVER_SCROLL_NEVER
      viewModel.harmonyViewModel.beatAdapter = HarmonyBeatAdapter(viewModel, this)
      adapter = viewModel.harmonyViewModel.beatAdapter
      /*adapter.registerAdapterDataObserver(
        object : RecyclerView.AdapterDataObserver() {
          override fun onItemRangeInserted(start: Int, count: Int) {
            //updateEmptyViewVisibility(this@recyclerView)
          }

          override fun onItemRangeRemoved(start: Int, count: Int) {
            //updateEmptyViewVisibility(this@recyclerView)
          }
        })*/
    }.lparams(matchParent, matchParent) {
      marginStart = marginForKey - dip(5)
    }

    init()
    post {
      syncScrollingChordText()
    }
  }
}
