package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.zoomableRecyclerView
import org.jetbrains.anko.*

class HarmonyView(
  context: Context,
  val viewModel: PaletteViewModel,
  init: HideableRelativeLayout.() -> Unit = {}
) : HideableRelativeLayout(context) {
  init {
    viewModel.harmonyView = this
    backgroundColor = color(R.color.colorPrimaryDark)
    viewModel.harmonyViewModel.harmonyElementRecycler = zoomableRecyclerView {
      id = R.id.center_h_scroller
      isFocusableInTouchMode = true
      clipChildren = false
      clipToPadding = false
    }.lparams {
      width = ViewGroup.LayoutParams.MATCH_PARENT
      height = ViewGroup.LayoutParams.WRAP_CONTENT
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
  }
}
//}

