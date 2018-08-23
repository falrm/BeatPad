package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.applyToHolders
import com.jonlatane.beatpad.util.layoutWidth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dimen
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.wrapContent

class HarmonyChordAdapter(
	val viewModel: HarmonyViewModel,
	val recyclerView: _RecyclerView
) : RecyclerView.Adapter<HarmonyChordHolder>(), AnkoLogger {

  @Volatile
  var elementWidth = recyclerView.run { dimen(R.dimen.subdivision_controller_size) }
    @Synchronized set(value) {
      field = value
      recyclerView.applyToHolders<HarmonyChordHolder> {
        it.element.layoutWidth = field
      }
    }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HarmonyChordHolder {
		return with(recyclerView) {
			HarmonyChordHolder(
				viewModel = viewModel,
				element = HarmonyElementView(context, viewModel = viewModel).lparams {
					width = elementWidth
					height = wrapContent
				},
				adapter = this@HarmonyChordAdapter
			)
		}
	}

	override fun onBindViewHolder(holder: HarmonyChordHolder, elementPosition: Int) {
    holder.element.setAllParentsClip(false)
		holder.element.elementPosition = elementPosition
    holder.element.layoutWidth = elementWidth
		holder.element.invalidate()
	}

	override fun getItemCount(): Int = viewModel.harmony?.elements?.size ?: 0
}