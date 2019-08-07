package com.jonlatane.beatpad.util

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView

object InstaRecycler {
  val example_id = 775839

  /**
   * @param holderViewFactory must be used by any custom [holderFactory]. If not, parameter is ignored.
   */
  fun <T: _RecyclerView> instaRecycler(
    context: Context,
    factory: (context: Context) -> T,
    holderViewFactory: T.() -> View = {
      TextView(this.context)
        .apply {
          id = example_id
          textSize = 16f
          padding = dip(15f)
          typeface = MainApplication.chordTypeface
        }.lparams(matchParent, wrapContent)
    },
    holderFactory: T.() -> RecyclerView.ViewHolder = {
      val view = holderViewFactory()
      object: RecyclerView.ViewHolder(view), SmartAdapter.Holder {
        override fun updateSmartHolder() {
          if(adapterPosition >= 0) binder(adapterPosition)
        }
      }
    },
    itemCount: () -> Int = { 7 },
    binder: View.(Int) -> Unit = { position ->
      findViewById<TextView>(example_id).text = "Item View $position"
    },
    orientation: Int = LinearLayoutManager.VERTICAL,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context, orientation, false).apply {
      isItemPrefetchEnabled = false
    },
    overScrollMode: Int = View.OVER_SCROLL_NEVER
  ): T {
    val recyclerView = factory(context).also {
      it.layoutManager = layoutManager
      it.overScrollMode = overScrollMode
    }
    val adapter = object: SmartAdapter<RecyclerView.ViewHolder>() {
      override fun getItemCount(): Int = itemCount()

      override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.binder(position);
      }

      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
        = recyclerView.holderFactory()
    }
    recyclerView.adapter = adapter
    return recyclerView
  }
}