package com.jonlatane.beatpad.util.smartrecycler

import android.support.v7.widget.RecyclerView

abstract class SmartAdapter<HolderType: RecyclerView.ViewHolder>: RecyclerView.Adapter<HolderType>() {
  private val _boundViewHolders = mutableSetOf<HolderType>()
  val boundViewHolders: Set<HolderType> = _boundViewHolders

  override fun onBindViewHolder(holder: HolderType, position: Int) {
    _boundViewHolders += holder
  }

  override fun onViewRecycled(holder: HolderType) {
    super.onViewRecycled(holder)
    _boundViewHolders -= holder
  }

  fun updateSmartHolders() {
    boundViewHolders.mapNotNull { it as? Holder }.forEach { it.updateSmartHolder() }
  }

  fun applyToHolders(
    mutation: (HolderType) -> Unit
  ) {
    boundViewHolders.forEach { mutation(it) }
  }

  interface Holder {
    fun updateSmartHolder()
  }
}