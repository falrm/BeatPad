package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.util.smartrecycler.firstVisibleItemPosition

interface BeatAdapter {
  var elementWidth: Int
  val recyclerView: RecyclerView

  fun invalidate(beatPosition: Int)

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