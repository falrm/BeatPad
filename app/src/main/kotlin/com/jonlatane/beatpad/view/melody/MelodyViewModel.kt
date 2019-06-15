package com.jonlatane.beatpad.view.melody

import android.animation.ValueAnimator
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingModifiers
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingToolbar
import com.jonlatane.beatpad.view.melody.toolbar.MelodyReferenceToolbar
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
		updateToolbarsAndMelody()
	}
	val melodyReference get() = openedMelody?.let { melody ->
		BeatClockPaletteConsumer.section?.let { section ->
			section.melodies.firstOrNull { it.melody == melody }
				?: Section.MelodyReference(melody).also { section.melodies.add(it) }
		}
	}
	val isMelodyReferenceEnabled: Boolean get()  = melodyReference != null && !melodyReference!!.isDisabled

	val harmony: Harmony? get() = paletteViewModel.harmonyViewModel.harmony

	val playing = AtomicBoolean(false)
	var verticalAxis: MelodyToneAxis? = null
	lateinit var melodyReferenceToolbar: MelodyReferenceToolbar
	lateinit var melodyEditingToolbar: MelodyEditingToolbar
	lateinit var melodyView: HideableRelativeLayout
	lateinit var melodyLeftScroller: NonDelayedScrollView
	lateinit var melodyEditingModifiers: MelodyEditingModifiers
	lateinit var melodyVerticalScrollView: NonDelayedScrollView
	lateinit var melodyRecyclerView: NonDelayedRecyclerView
	lateinit var beatAdapter: MelodyBeatAdapter
	fun updateToolbarsAndMelody() {
		beatAdapter.notifyDataSetChanged()
		melodyReferenceToolbar.updateButtonText()
		melodyEditingToolbar.updateButtonText()
	}

	enum class DisplayType { COLORBLOCK, NOTATION }
	enum class LayoutType { LINEAR, GRID }
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
		melodyReferenceToolbar.displayTypeButton.imageResource = when(displayType) {
			DisplayType.COLORBLOCK -> R.drawable.notehead_filled
			else -> R.drawable.colorboard_icon_vertical
		}
	}

	private var layoutTypeDimensions = mutableMapOf<LayoutType, Pair<Int, Int>>()
	var layoutType = LayoutType.LINEAR
		set(value) = with(beatAdapter) {
			layoutTypeDimensions[field] = elementWidth to elementHeight
			field = value
			val layoutTypeDimensions = layoutTypeDimensions[value]
			info("Melody layout type: $value")
			if(value == LayoutType.GRID) {
				gridLayout()
				layoutTypeDimensions?.let { (width, height) ->
					animateElementHeight(height)
					animateElementWidth(width)
				} ?: if(elementHeight > 2f/3 * melodyVerticalScrollView.height) {
					animateElementHeight(round(2f/3 * melodyVerticalScrollView.height).toInt()) {
						onZoomFinished()
					}
				} else {
					onZoomFinished()
				}
			} else {
				linearLayout()
				layoutTypeDimensions?.let { (width, height) ->
					animateElementHeight(height)
					animateElementWidth(width)
				} ?: if(elementHeight < 2f/3 * melodyVerticalScrollView.height) {
					animateElementHeight(round(2f/3 * melodyVerticalScrollView.height).toInt())
				}
			}
			melodyReferenceToolbar.layoutTypeButton.imageResource = when(layoutType) {
				LayoutType.GRID -> R.drawable.line
				else -> R.drawable.grid
			}
		}

	/**
	 * Exposed for access by [HarmonyView]
	 */
	fun onZoomFinished() = with(beatAdapter) {
		val targetWidth = if(layoutType == LayoutType.GRID) {
			round(melodyVerticalScrollView.width.toFloat() / recommendedSpanCount).toInt()
				.also { animateElementWidth(it) }
		} else elementWidth

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



	fun enableMelodyReference(melody: Melody<*>, melodyReference: Section.MelodyReference?) {
		if(melodyReference == null) {
			BeatClockPaletteConsumer.section?.melodies?.add(
				Section.MelodyReference(melody, 0.5f, Section.PlaybackType.Indefinite)
			)
		} else if(melodyReference.isDisabled) {
			melodyReference.playbackType = Section.PlaybackType.Indefinite
		}
		if(melody == openedMelody) {
			updateToolbarsAndMelody()
		}
	}

	fun disableMelodyReference(melody: Melody<*>, melodyReference: Section.MelodyReference) {
		melodyReference.playbackType = Section.PlaybackType.Disabled
		// Sanitization: Remove duplicates
		BeatClockPaletteConsumer.section?.melodies?.removeAll {
			it.melody == melody && it != melodyReference
		}
		if(melody == openedMelody) {
			updateToolbarsAndMelody()
		}
	}
}