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
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import org.jetbrains.anko.*

class HarmonyView(
  context: Context,
  val viewModel: HarmonyViewModel,
  init: HideableRelativeLayout.() -> Unit = {}
) : HideableRelativeLayout(context) {
  val harmonyElementRecycler: NonDelayedRecyclerView
  init {
    viewModel.harmonyView = this
    backgroundColor = color(R.color.colorPrimaryDark)
    harmonyElementRecycler = nonDelayedRecyclerView {
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
      overScrollMode = View.OVER_SCROLL_NEVER
      viewModel.chordAdapter = HarmonyChordAdapter(viewModel, this)
      adapter = viewModel.chordAdapter
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

