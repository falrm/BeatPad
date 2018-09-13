package com.jonlatane.beatpad.view.beat

import android.support.v7.widget.RecyclerView
import android.view.View

class MelodyBeatHolder<ViewType>
(
  val viewModel: BeatViewModel,
  val element: ViewType,
  val adapter: BeatAdapter<*, ViewType>
) : RecyclerView.ViewHolder(element)
  where ViewType : BeatView, ViewType : View {
  private val context get() = element.context

  init {
    element.viewModel = viewModel
  }
}