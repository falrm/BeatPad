package com.jonlatane.beatpad.util

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.view.palette.PartHolder

@Suppress("UNCHECKED_CAST")
@Synchronized
fun <HolderType: RecyclerView.ViewHolder>RecyclerView.applyToHolders(
	mutation: (HolderType) -> Unit
) {
  viewHolders<HolderType>()
    .forEach { mutation(it) }
  adapter.notifyDataSetChanged()
}

@Suppress("UNCHECKED_CAST")
@Synchronized
fun <HolderType: RecyclerView.ViewHolder>RecyclerView.viewHolders()
  = (0 until adapter.itemCount).
    mapNotNull { findViewHolderForAdapterPosition(it) as? HolderType }