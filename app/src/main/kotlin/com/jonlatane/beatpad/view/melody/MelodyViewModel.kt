package com.jonlatane.beatpad.view.melody

import android.animation.ValueAnimator
import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.util.smartrecycler.applyToHolders
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.NonDelayedRecyclerView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingModifiers
import com.jonlatane.beatpad.view.melody.toolbar.MelodyEditingToolbar
import com.jonlatane.beatpad.view.melody.toolbar.MelodyReferenceToolbar
import com.jonlatane.beatpad.view.melody.toolbar.SectionToolbar
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.ceil
import kotlin.math.round
import kotlin.properties.Delegates.observable

class MelodyViewModel(
  val paletteViewModel: PaletteViewModel
): AnkoLogger {
	var openedMelody: Melody<*>? by observable<Melody<*>?>(null) { _, _, _ ->
		updateToolbarsAndMelody()
	}
	var openedPart: Part? by observable<Part?>(null) { _, _, _ ->
		updateToolbarsAndMelody()
	}
	enum class SectionLayoutType { SINGLE_SECTION, FULL_PALETTE }
	var sectionLayoutType: SectionLayoutType by observable(SectionLayoutType.SINGLE_SECTION) { _, _, _ ->
		updateToolbarsAndMelody()
	}
	val melodyReference get() = openedMelody?.let { melody ->
		BeatClockPaletteConsumer.section?.let { section ->
			section.melodies.firstOrNull { it.melody == melody }
				?: Section.MelodyReference(melody, playbackType = Section.PlaybackType.Disabled)
					.also { section.melodies.add(it) }
		}
	}
	val isMelodyReferenceEnabled: Boolean get()  = melodyReference != null && !melodyReference!!.isDisabled

	val harmony: Harmony? get() = paletteViewModel.harmonyViewModel.harmony

	val playing = AtomicBoolean(false)
	var verticalAxis: MelodyToneAxis? = null
	lateinit var sectionToolbar: SectionToolbar
	lateinit var melodyReferenceToolbar: MelodyReferenceToolbar
	lateinit var melodyEditingToolbar: MelodyEditingToolbar
	lateinit var melodyView: HideableRelativeLayout
	lateinit var melodyLeftScroller: NonDelayedScrollView
	lateinit var melodyEditingModifiers: MelodyEditingModifiers
	lateinit var melodyVerticalScrollView: NonDelayedScrollView
	lateinit var melodyRecyclerView: NonDelayedRecyclerView
	lateinit var beatAdapter: MelodyBeatAdapter
	fun updateToolbarsAndMelody() {
		updateMelodyDisplay()
		if(openedMelody != null) {

			sectionToolbar.hide()
			melodyReferenceToolbar.show()
			melodyReferenceToolbar.updateButtonText()
			melodyEditingToolbar.updateButtonText()
		} else {
			sectionToolbar.backgroundColor = BeatClockPaletteConsumer.currentSectionColor
			sectionToolbar.show()
			melodyReferenceToolbar.hide()
			melodyEditingToolbar.hide()
			melodyEditingModifiers.hide()
		}
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
		melodyReferenceToolbar.displayTypeButton.imageResource = when(value) {
			DisplayType.COLORBLOCK -> R.drawable.colorboard_icon_vertical
			else -> R.drawable.notehead_filled
		}
	}


	private var layoutTypeDimensions = mutableMapOf<LayoutType, Pair<Int, Int>>()
	var layoutType = LayoutType.LINEAR
		set(value) = with(beatAdapter) {
			layoutTypeDimensions[field] = elementWidth to elementHeight
			field = value
			info("Melody layout type: $value")
			if(value == LayoutType.GRID) {
				gridLayout()
			} else {
				linearLayout()
			}
			melodyReferenceToolbar.layoutTypeButton.imageResource = when(value) {
				LayoutType.GRID -> R.drawable.grid
				else -> R.drawable.line
			}
		}

	fun restoreLayoutTypeDimensions() = with(beatAdapter) {
		val layoutTypeDimensions = layoutTypeDimensions[layoutType] ?: elementWidth to elementHeight
		layoutTypeDimensions.let { (width, height) ->
			animateElementHeight(height)
			animateElementWidth(width) {
				onZoomFinished()
			}
		}
	}

	/**
	 * Exposed for access by [HarmonyView]
	 */
	fun onZoomFinished(animated: Boolean = true) = with(beatAdapter) {
		val targetWidth = if(layoutType == LayoutType.GRID) {
			ceil(melodyVerticalScrollView.width.toFloat() / recommendedSpanCount).toInt()
				.also {
					if(animated) animateElementWidth(it)
					else elementWidth =  it
				}
		} else elementWidth

		// Align height for notation against target width
		if(displayType == DisplayType.NOTATION) {
			val targetHeight = when {
				elementHeight > 5 * targetWidth -> 5 * targetWidth
				elementHeight < 0.5f * targetWidth -> (0.5f * targetWidth).toInt()
				else -> elementHeight
			}
			if(animated) animateElementHeight(targetHeight)
			else elementHeight = targetHeight
		}
	}

	internal fun redraw() {
		beatAdapter.notifyDataSetChanged()
		verticalAxis?.invalidate()
	}



	fun enableMelodyReference(
    melody: Melody<*>,
    melodyReference: Section.MelodyReference?,
    section: Section = BeatClockPaletteConsumer.section!!
  ) {
		if(melodyReference == null) {
      section.melodies.add(
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

	fun updateMelodyDisplay() {
		melodyRecyclerView.applyToHolders<RecyclerView.ViewHolder> {
			it.itemView.childrenRecursiveSequence().forEach { it.invalidate() }
		}
	}
}