package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.util.applyToHolders
import com.jonlatane.beatpad.util.layoutWidth
import com.jonlatane.beatpad.view.melody.MelodyBeatAdapter.Companion.initialBeatWidthDp
import com.jonlatane.beatpad.view.melody.MelodyBeatAdapter.Companion.minimumBeatWidthDp
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7._RecyclerView

class HarmonyBeatAdapter(
	val viewModel: PaletteViewModel,
	val recyclerView: _RecyclerView
) : RecyclerView.Adapter<HarmonyBeatHolder>(), AnkoLogger {
  private val minimumElementWidth: Int = recyclerView.run { dip(minimumBeatWidthDp) }

  @Volatile
  var elementWidth = recyclerView.run { dip(initialBeatWidthDp) }
    @Synchronized set(value) {
			if (field != value) {
        field = when {
          value > minimumElementWidth -> {
            value
          }
          else -> minimumElementWidth
        }
				recyclerView.applyToHolders<HarmonyBeatHolder> {
					it.element.layoutWidth = field
				}
				(viewModel as? PaletteViewModel)?.melodyViewModel?.beatAdapter?.elementWidth = field
      }
			viewModel.harmonyViewModel.harmonyView?.syncScrollingChordText()
    }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HarmonyBeatHolder {
		return with(recyclerView) {
			HarmonyBeatHolder(
				viewModel = viewModel.harmonyViewModel,
				element = HarmonyBeatView(context, viewModel = viewModel.harmonyViewModel).lparams {
					width = elementWidth
					height = matchParent//dip(45)
				},
				adapter = this@HarmonyBeatAdapter
			)
		}
	}

  fun invalidate(beatPosition: Int) {
    recyclerView.layoutManager.findViewByPosition(beatPosition)?.invalidate()
  }

	override fun onBindViewHolder(holder: HarmonyBeatHolder, beatPosition: Int) {
		holder.element.beatPosition = beatPosition
    holder.element.layoutWidth = elementWidth
		holder.element.invalidate()
	}

	override fun getItemCount(): Int = viewModel.harmonyViewModel.harmony?.let { harmony ->
		Math.ceil(harmony.length.toDouble() / harmony.subdivisionsPerBeat).toInt()
	}?: 16 // Always render at least one item, for layout sanity. 16 is kind of a hack though.
}