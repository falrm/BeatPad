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
fun <HolderType : RecyclerView.ViewHolder> RecyclerView.viewHolders(): List<HolderType>
  = (0 until childCount)
  .map { getChildAt(it) }
  .map {
//    try {
      getChildViewHolder(it)
//    } catch(t: Throwable) {
//      null
//    }
  }.map { it as HolderType }

// (0 until adapter.itemCount).
// mapNotNull { findViewHolderForAdapterPosition(it) as? HolderType }

//  for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
//  final ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
//  ...
//}