package com.jonlatane.beatpad.view.melody

import android.animation.ValueAnimator
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.info
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.round
import kotlin.properties.Delegates.observable

class MelodyViewModel(
  val paletteViewModel: PaletteViewModel
): AnkoLogger {
	var openedMelody by observable<Melody<*>?>(PaletteStorage.baseMelody) { _, _, _ ->
		beatAdapter.notifyDataSetChanged()
    melodyToolbar.updateButtonText()
	}
	val playing = AtomicBoolean(false)
	var verticalAxis: MelodyToneAxis? = null
	lateinit var melodyToolbar: MelodyToolbar
	lateinit var melodyView: HideableRelativeLayout
	lateinit var melodyLeftScroller: NonDelayedScrollView
	lateinit var melodyEditingModifiers: MelodyEditingModifiers
	lateinit var melodyVerticalScrollView: NonDelayedScrollView
	lateinit var melodyRecyclerView: NonDelayedRecyclerView
	lateinit var beatAdapter: MelodyBeatAdapter

	enum class DisplayType { COLORBLOCK, NOTATION }
	var displayType = DisplayType.NOTATION
	set(value) {
		field = value
    info("Melody display type: $value")
		val fadeInAnim: ValueAnimator
		val fadeOutAnim: ValueAnimator
		if(value == DisplayType.COLORBLOCK) {
			fadeInAnim = ValueAnimator.ofFloat(beatAdapter.colorblockAlpha, 1f)
			fadeOutAnim = ValueAnimator.ofFloat(beatAdapter.notationAlpha, 0f)
			fadeInAnim.addUpdateListener { valueAnimator ->
				beatAdapter.colorblockAlpha = valueAnimator.animatedValue as Float
			}
      fadeOutAnim.addUpdateListener { valueAnimator ->
				beatAdapter.notationAlpha = valueAnimator.animatedValue as Float
			}
		} else {
			fadeInAnim = ValueAnimator.ofFloat(beatAdapter.notationAlpha, 1f)
			fadeOutAnim = ValueAnimator.ofFloat(beatAdapter.colorblockAlpha, 0f)
			fadeInAnim.addUpdateListener { valueAnimator ->
				beatAdapter.notationAlpha = valueAnimator.animatedValue as Float
			}
      fadeOutAnim.addUpdateListener { valueAnimator ->
				beatAdapter.colorblockAlpha = valueAnimator.animatedValue as Float
			}
		}
		fadeInAnim.setDuration(500).start()
		fadeOutAnim.setDuration(500).start()
		melodyToolbar.displayTypeButton.imageResource = when(displayType) {
			DisplayType.COLORBLOCK -> R.drawable.notehead_filled
			else -> R.drawable.colorboard_icon_vertical
		}
	}

	/**
	 * Exposed for access by [HarmonyView]
	 */
	fun onZoomFinished() = with(beatAdapter) {
		val targetWidth = if(useGridLayoutManager) {
			round(melodyVerticalScrollView.width.toFloat() / recommendedSpanCount).toInt()
		} else elementWidth
		animateElementWidth(targetWidth)

		// Align height for notation against target width
		if(displayType == DisplayType.NOTATION) {
			val targetHeight = when {
				elementHeight > 5 * targetWidth -> 5 * targetWidth
				elementHeight < 0.5f * targetWidth -> (0.5f * targetWidth).toInt()
				else -> elementHeight
			}
			animateElementHeight(targetHeight)
		}
	}

	internal fun redraw() {
		beatAdapter.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}
}