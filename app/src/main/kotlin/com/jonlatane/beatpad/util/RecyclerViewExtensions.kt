package com.jonlatane.beatpad.util

import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.view.palette.PartHolder

@Suppress("UNCHECKED_CAST")
fun <HolderType: RecyclerView.ViewHolder>RecyclerView.applyToHolders(
	mutation: (HolderType) -> Unit
) {
	(0 until childCount)
		.map { getChildViewHolder(getChildAt(it)) as HolderType }
		.forEach(mutation)
}