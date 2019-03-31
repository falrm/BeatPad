package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.util.firstVisibleItemPosition

interface BeatAdapter {
  var elementWidth: Int
  val recyclerView: RecyclerView

  fun invalidate(beatPosition: Int) {
    recyclerView.layoutManager.findViewByPosition(beatPosition)?.invalidate()
  }

  fun syncPositionTo(to: RecyclerView) {
    val from = recyclerView
    val otherLayoutManager = to.layoutManager as LinearLayoutManager
    val offset = -from.computeHorizontalScrollOffset() % (to.adapter as BeatAdapter).elementWidth
    otherLayoutManager.scrollToPositionWithOffset(
      from.firstVisibleItemPosition,
      offset
    )
  }
}