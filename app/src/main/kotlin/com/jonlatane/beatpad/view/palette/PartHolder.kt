package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.widget.RelativeLayout
import android.widget.TextView

class PartHolder(
	val layout: RelativeLayout
) : RecyclerView.ViewHolder(layout) {
	companion object {
		val instrumentTextId = 1001
		val sequenceRecyclerId = 1002
	}
	//lateinit var instrumentText: TextView
	//lateinit var recycler: View
	val instrumentText by lazy { layout.findViewById<TextView>(instrumentTextId) }
	val recycler by lazy { layout.findViewById<RecyclerView>(sequenceRecyclerId) }
}