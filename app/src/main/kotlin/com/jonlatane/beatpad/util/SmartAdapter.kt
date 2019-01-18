package com.jonlatane.beatpad.util

import android.support.v7.widget.RecyclerView

abstract class SmartAdapter<HolderType: RecyclerView.ViewHolder>: RecyclerView.Adapter<HolderType>() {
  private val _boundViewHolders = mutableSetOf<HolderType>()
  val boundViewHolders: Set<HolderType> = _boundViewHolders

  override fun onBindViewHolder(holder: HolderType, partPosition: Int) {
    _boundViewHolders += holder
  }

  override fun onViewRecycled(holder: HolderType) {
    super.onViewRecycled(holder)
    _boundViewHolders -= holder
  }
}