package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.*
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.coroutines.onScrollChange
import org.jetbrains.anko.sdk25.coroutines.onTouch


inline fun ViewManager.melodyView(
	theme: Int = 0,
	viewModel: PaletteViewModel,
	init: HideableRelativeLayout.() -> Unit
)
	= with(viewModel.melodyViewModel) {
	ankoView({
		melodyView = HideableRelativeLayout(it).apply {
			backgroundColor = context.color(R.color.white)
			melodyReferenceToolbar = melodyReferenceToolbar(viewModel) {
				id = R.id.melody_reference_toolbar
			}.lparams(matchParent, wrapContent) {
				alignParentTop()
			}
			melodyEditingToolbar = melodyEditingToolbar(viewModel) {
				id = R.id.melody_editing_toolbar
			}.lparams(matchParent, wrapContent) {
				below(melodyReferenceToolbar)
			}
			melodyEditingModifiers = melodyEditingModifiers {
				id = R.id.bottom_scroller
				onHeldDownChanged = { heldDown ->
					//if (heldDown) holdToEdit?.animate()?.alpha(0f)?.translationY(100f)
					//else holdToEdit?.animate()?.alpha(1f)?.translationY(0f)
					melodyRecyclerView.scrollingEnabled = !heldDown
					melodyVerticalScrollView.scrollingEnabled = !heldDown
				}
			}.lparams(matchParent, melodyReferenceToolbar.squareSize) {
				alignParentBottom()
				alignParentRight()
				alignParentLeft()
			}
			melodyLeftScroller = nonDelayedScrollView {
				id = R.id.left_scroller
				linearLayout {
					verticalAxis = melodyToneAxis().lparams {
						width = dip(30)
						height = dip(MelodyBeatAdapter.initialBeatHeightDp)
					}
				}
				scrollingEnabled = false
				isVerticalScrollBarEnabled = false
			}.lparams(dip(30), matchParent) {
				below(melodyEditingToolbar)
				above(melodyEditingModifiers)
				alignParentLeft()
			}
			melodyVerticalScrollView = nonDelayedScrollView {
				id = R.id.center_v_scroller
				onScrollChange { _, _, scrollY, _, _ ->
					melodyLeftScroller.scrollY = scrollY
				}

				melodyRecyclerView = zoomableRecyclerView {
					id = R.id.center_h_scroller
					isFocusableInTouchMode = true


					zoomHandler = { xDelta, yDelta ->
						AnkoLogger<MelodyViewModel>().verbose("Zooming: xDelta=$xDelta, yDelta=$yDelta")
						when {
							(xDelta.toInt() != 0 || yDelta.toInt() != 0) -> {
								viewModel.melodyBeatAdapter.apply {
                  when(layoutType) {
                    MelodyViewModel.LayoutType.GRID -> {
											if (elementHeight + (10f * yDelta).toInt() >= maximumRecommendedElementHeightForOverview) {
												layoutType = MelodyViewModel.LayoutType.LINEAR
												//onZoomFinished()
											}
										}
                    MelodyViewModel.LayoutType.LINEAR -> {
                      if(elementHeight + (10f * yDelta).toInt() <= minimumRecommendedElementHeightForEditing) {
												layoutType = MelodyViewModel.LayoutType.GRID
												//onZoomFinished()
											}
                    }
                  }
									elementWidth += xDelta.toInt()
									elementHeight += (10f * yDelta).toInt()
//									viewModel.melodyViewModel.updateMelodyDisplay()
//									notifyDataSetChanged()
								}
								true
							}
							else                                         -> false
						}
					}

					zoomFinishedHandler = { onZoomFinished() }
					layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
						isItemPrefetchEnabled = false
					}
					overScrollMode = View.OVER_SCROLL_NEVER
					viewModel.melodyBeatAdapter = MelodyBeatAdapter(this@with, this)
					adapter = viewModel.melodyBeatAdapter
					adapter.registerAdapterDataObserver(
						object : RecyclerView.AdapterDataObserver() {
							override fun onItemRangeInserted(start: Int, count: Int) {
								//updateEmptyViewVisibility(this@recyclerView)
							}

							override fun onItemRangeRemoved(start: Int, count: Int) {
								//updateEmptyViewVisibility(this@recyclerView)
							}
						})
				}.lparams {
					height = wrapContent
					width = matchParent
				}
			}.lparams {
				width = ViewGroup.LayoutParams.MATCH_PARENT
				height = ViewGroup.LayoutParams.MATCH_PARENT
				alignParentRight()
				above(melodyEditingModifiers)
				rightOf(melodyLeftScroller)
				below(melodyEditingToolbar)
			}

			onTouch(returnValue = true) { _, _ -> Unit }

			post {
				melodyEditingToolbar.hide(false)
				melodyEditingModifiers.hide(false)
			}
		}
		viewModel.melodyView
	}, theme, init)
}
//}

