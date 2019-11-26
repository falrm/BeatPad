package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import com.jonlatane.beatpad.view.harmony.HarmonyBeatView
import com.jonlatane.beatpad.view.melody.renderer.BaseMelodyBeatRenderer.ViewType
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.wrapContent

class MelodyBeatHolder private constructor(
	val viewModel: MelodyViewModel,
	val melodyBeatViews: List<MelodyBeatView>,
	val harmonyBeatView: HarmonyBeatView,
	val element: View, //base view
	private val adapter: MelodyBeatAdapter
) : RecyclerView.ViewHolder(element), SmartAdapter.Holder {
	companion object {
		fun create(
			recyclerView: _RecyclerView,
			adapter: MelodyBeatAdapter
		) = with(adapter) {
			with(recyclerView) {
				val melodyBeatViews = listOf(
					MelodyBeatView(context, viewModel = viewModel)
						.apply { viewType = ViewType.OtherNonDrumParts }
						.lparams(elementWidth, elementHeight),
					MelodyBeatView(context, viewModel = viewModel)
						.apply {
							viewType =
								//palette.parts.find { it.drumTrack }?.let { MelodyBeatRenderer.ViewType.PartView(it) } ?:
									ViewType.DrumPart
						}.lparams(elementWidth, elementHeight),
					MelodyBeatView(context, viewModel = viewModel)
						.apply { viewType = ViewType.Unused }
						.lparams(elementWidth, 0)
//					,
//					MelodyBeatView(context, viewModel = viewModel)
//						.apply { viewType = MelodyBeatRenderer.ViewType.Unused }
//						.lparams(elementWidth, 0),
//					MelodyBeatView(context, viewModel = viewModel)
//						.apply { viewType = MelodyBeatRenderer.ViewType.Unused }
//						.lparams(elementWidth, 0),
//					MelodyBeatView(context, viewModel = viewModel)
//						.apply { viewType = MelodyBeatRenderer.ViewType.Unused }
//						.lparams(elementWidth, 0),
//					MelodyBeatView(context, viewModel = viewModel)
//						.apply { viewType = MelodyBeatRenderer.ViewType.Unused }
//						.lparams(elementWidth, 0),
//					MelodyBeatView(context, viewModel = viewModel)
//						.apply { viewType = MelodyBeatRenderer.ViewType.Unused }
//						.lparams(elementWidth, 0)
					)
				val harmonyBeatView = HarmonyBeatView(
					context,
					viewModel = viewModel.paletteViewModel.harmonyViewModel
				).lparams(elementWidth, harmonyViewHeight)
				MelodyBeatHolder(
					viewModel,
					melodyBeatViews = melodyBeatViews,
					harmonyBeatView = harmonyBeatView,
					element = _LinearLayout(
						context
					).apply {
						orientation = LinearLayout.VERTICAL
						addView(harmonyBeatView)
						melodyBeatViews.forEach { melodyBeatView ->
							addView(melodyBeatView)
						}
					}.lparams(wrapContent, wrapContent),
					adapter = adapter
				)
			}
		}
	}
	override fun updateSmartHolder() {
		adapter.onBindViewHolder(this, adapterPosition)
	}

	private val context get() = harmonyBeatView.context
}