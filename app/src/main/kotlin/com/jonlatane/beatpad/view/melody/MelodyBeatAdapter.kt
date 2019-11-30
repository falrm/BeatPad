package com.jonlatane.beatpad.view.melody

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.dsl.Patterns
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import com.jonlatane.beatpad.util.smartrecycler.applyToHolders
import com.jonlatane.beatpad.view.melody.renderer.BaseMelodyBeatRenderer
import com.jonlatane.beatpad.view.melody.renderer.BaseMelodyBeatRenderer.ViewType
import com.jonlatane.beatpad.view.melody.renderer.MelodyBeatRenderer
import com.jonlatane.beatpad.view.palette.BeatScratchToolbar
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round


class MelodyBeatAdapter(
  val viewModel: MelodyViewModel,
  override val recyclerView: _RecyclerView
) : SmartAdapter<MelodyBeatHolder>(), AnkoLogger, BeatAdapter, Patterns {
  companion object {
    const val initialBeatWidthDp: Float = 125f
    const val initialBeatHeightDp: Float = 400f
    const val minimumBeatWidthDp: Float = 30f
    const val maximumBeatHeightDp: Float = 10000f
    fun sectionAndStartBeat(palette: Palette, beatPosition: Int) = palette.sections.fold<Section, Pair<Section?, Int>>(null to 0) { (sectionAtPosition: Section?, sum), section ->
      val sectionBeatLength = section.harmony.run { length / subdivisionsPerBeat }
      when {
        sum + sectionBeatLength <= beatPosition -> section to sum + sectionBeatLength
        else                                    -> sectionAtPosition to sum
      }
    }
  }

  private val axis get() = viewModel.verticalAxis!!
  private val axes: List<MelodyToneAxis> get() = viewModel.verticalAxes
  val minimumElementWidth get() = recyclerView.run { dip(minimumBeatWidthDp) }
  val maximumElementWidth
    get() = when (viewModel.layoutType) {
      MelodyViewModel.LayoutType.GRID   -> viewModel.melodyVerticalScrollView.width / 2
      MelodyViewModel.LayoutType.LINEAR -> viewModel.melodyVerticalScrollView.width * 4
    }
  val minimumElementHeight
    get() = when (viewModel.layoutType) {
      MelodyViewModel.LayoutType.LINEAR -> minimumRecommendedElementHeightForEditing
      MelodyViewModel.LayoutType.GRID   -> recyclerView.run { dip(100) }
    }
  val maximumElementHeight: Int
    get() = when (viewModel.layoutType) {
      MelodyViewModel.LayoutType.LINEAR -> recyclerView.run { dip(maximumBeatHeightDp) }
      MelodyViewModel.LayoutType.GRID   -> maximumRecommendedElementHeightForOverview
    }
  val minimumRecommendedElementHeightForEditing
    get() = when {
      viewModel.paletteViewModel.storageContext.configuration.tablet -> (viewModel.melodyVerticalScrollView.height * 5f / 12f).toInt()
      else                                                           -> (viewModel.melodyVerticalScrollView.height * 7f / 12f).toInt()
    }
  val maximumRecommendedElementHeightForOverview
    get() = when {
      viewModel.paletteViewModel.storageContext.configuration.tablet -> (viewModel.melodyVerticalScrollView.height * 7f / 12f).toInt()
      else                                                           -> (viewModel.melodyVerticalScrollView.height * 5f / 12f).toInt()
    }


  override var elementWidth: Int = recyclerView.run { dip(initialBeatWidthDp) }
    set(value) {
      if (field != value) {
        field = when {
          value < minimumElementWidth -> {
            minimumElementWidth
          }
          value > maximumElementWidth -> {
            maximumElementWidth
          }
          else                        -> value
        }
        verbose("Setting width to $field")
        recyclerView.applyToHolders<MelodyBeatHolder> {
          it.melodyBeatViews.applyToEach { layoutWidth = field }
        }
        if (
          viewModel.layoutType == MelodyViewModel.LayoutType.GRID
          && (recyclerView.layoutManager as? GridLayoutManager)?.spanCount != recommendedSpanCount
        ) {
          val state = (recyclerView.layoutManager as LinearLayoutManager).onSaveInstanceState()
          recyclerView.layoutManager = recommendedGridLayoutManager()
          (recyclerView.layoutManager as LinearLayoutManager).onRestoreInstanceState(state)
        }
        viewModel.melodyView.syncScrollingChordText()
      }
      //viewModel.paletteViewModel.harmonyViewModel.beatAdapter.elementWidth = field
    }

  var elementHeight = recyclerView.run { dip(initialBeatHeightDp) }
    set(value) {
      if (field != value) {
        field = when {
          value < minimumElementHeight -> {
            minimumElementHeight
          }
          value > maximumElementHeight -> {
            maximumElementHeight
          }
          else                         -> value
        }

        verbose("Setting height to $field")
        fun sendStuff() = recyclerView.run {
          applyToHolders<MelodyBeatHolder> {
            it.harmonyBeatView.forceLayout()
            it.melodyBeatViews.applyToEach { forceLayout() }
            it.itemView.requestLayout()
            it.itemView.forceLayout() //TODO: This helps
          }
          recyclerView.requestLayout() //TODO: This helps
          if (viewModel.layoutType === MelodyViewModel.LayoutType.LINEAR) {
            viewModel.melodyVerticalScrollView.requestLayout()
          }
        }
        val axesAreUsed = mutableListOf<Boolean?>(null, null, null)
        recyclerView.applyToHolders<MelodyBeatHolder> {
          it.harmonyBeatView.layoutHeight = recyclerView.harmonyViewHeight
          it.melodyBeatViews.applyToEachIndexed { index ->
            val isUsed = viewType.isUsed
            layoutHeight = if (isUsed) field else 0
            if(axesAreUsed[index] == null) {
              axesAreUsed[index] = isUsed
            }
          }
        }
        sendStuff()
        recyclerView.post { sendStuff() }
        //recyclerView.adapter.notifyDataSetChanged()
        //recyclerView.adapter.notifyItemRangeChanged(0, recyclerView.adapter.itemCount)
        axes[0].topMargin = recyclerView.harmonyViewHeight
        axesAreUsed.forEachIndexed { index, isUsed ->
          axes[index].layoutHeight = if(isUsed == true) field else 0
        }
        viewModel.melodyView.syncScrollingChordText()
      }
    }
  val recommendedSpanCount: Int
    get() = max(1, round(viewModel.melodyVerticalScrollView.width.toFloat() / elementWidth).toInt())
  private val linearLayoutManager = LinearLayoutManager(
    recyclerView.context,
    RecyclerView.HORIZONTAL,
    false
  ).apply {
    isItemPrefetchEnabled = false
  }

  private fun recommendedGridLayoutManager() = GridLayoutManager(
    recyclerView.context,
    recommendedSpanCount,
    RecyclerView.VERTICAL,
    false
  ).apply {
    //isItemPrefetchEnabled = false
  }

  fun linearLayout() = with(viewModel) {
    val state = (recyclerView.layoutManager as LinearLayoutManager).onSaveInstanceState()
    melodyVerticalScrollView.removeView(recyclerView)
    melodyView.removeView(recyclerView)
    with(melodyVerticalScrollView) {
      addView(recyclerView)
      recyclerView.lparams(matchParent, wrapContent)
    }
    recyclerView.layoutManager = linearLayoutManager
    (recyclerView.layoutManager as LinearLayoutManager).onRestoreInstanceState(state)
    melodyVerticalScrollView.scrollingEnabled = true
    melodyLeftScroller.show(animation = HideAnimation.HORIZONTAL)
    melodyView.rightSpacer.animateWidth(0)
    melodyView.leftSpacer.animateWidth(0)
  }

  fun gridLayout() = with(viewModel) {
    val state = (recyclerView.layoutManager as LinearLayoutManager).onSaveInstanceState()
    melodyVerticalScrollView.removeView(recyclerView)
    melodyView.removeView(recyclerView)
    with(melodyView) {
      addView(recyclerView)
      recyclerView.lparams(matchParent, matchParent) {
        melodyPosition()
      }
    }
    recyclerView.layoutManager = recommendedGridLayoutManager()
    (recyclerView.layoutManager as LinearLayoutManager).onRestoreInstanceState(state)
    melodyVerticalScrollView.scrollingEnabled = false
    melodyVerticalScrollView.verticalScrollbarPosition = 0
    val doZoomFinished = incrementUntil(2) {
      onZoomFinished()
    }
    melodyLeftScroller.hide(animation = HideAnimation.HORIZONTAL)
    melodyView.leftSpacer.animateWidth(viewModel.melodyView.dip(5)) { doZoomFinished() }
    melodyView.rightSpacer.animateWidth(viewModel.melodyView.dip(5)) { doZoomFinished() }
  }

  fun animateElementHeight(height: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
    val targetHeight = min(max(height, minimumElementHeight), maximumElementHeight)
    val anim = ValueAnimator.ofInt(this.elementHeight, targetHeight)
    anim.interpolator = LinearInterpolator()
    anim.addUpdateListener { valueAnimator ->
      this.elementHeight = valueAnimator.animatedValue as Int
    }
    endAction?.let {
      anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) = it()
      })
    }
    anim.setDuration(duration).start()
  }

  fun animateElementWidth(width: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
    val targetWidth = min(max(width, minimumElementHeight), maximumElementHeight)
    val anim = ValueAnimator.ofInt(this.elementWidth, width)
    anim.interpolator = LinearInterpolator()
    anim.addUpdateListener { valueAnimator ->
      this.elementWidth = valueAnimator.animatedValue as Int
    }
    endAction?.let {
      anim.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) = it()
      })
    }
    anim.setDuration(duration).start()
  }

  var colorblockAlpha: Float = 0f
    set(value) {
      field = value
      recyclerView.applyToHolders<MelodyBeatHolder> {
        it.melodyBeatViews.applyToEach {
          invalidateDrawingLayer()
        }
      }
    }

  var notationAlpha: Float = 1f
    set(value) {
      field = value
      recyclerView.applyToHolders<MelodyBeatHolder> {
        it.melodyBeatViews.applyToEach {
          invalidateDrawingLayer()
        }
      }
    }

  val RecyclerView.harmonyViewHeight get() = min(dip(45), elementHeight / 10)
  val MelodyBeatHolder.harmonyViewHeight get() = min(harmonyBeatView.dip(45), elementHeight / 10)
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyBeatHolder = MelodyBeatHolder.create(recyclerView, this)

  override fun onBindViewHolder(holder: MelodyBeatHolder, position: Int): Unit = with(holder) {
    super.onBindViewHolder(holder, position)
//    element.layoutWidth = elementWidth
//    element.layoutHeight = elementHeight + harmonyViewHeight
    val sectionStartBeatPosition = when (viewModel.paletteViewModel.interactionMode) {
      BeatScratchToolbar.InteractionMode.EDIT -> 0
      BeatScratchToolbar.InteractionMode.VIEW -> sectionAndStartBeat(
        viewModel.paletteViewModel.palette,
        position
      ).second
    }
    melodyBeatViews.applyToEach {
      beatPosition = position
      layoutWidth = elementWidth
      layoutHeight = if (viewType.isUsed == true) elementHeight else 0
      this.sectionStartBeatPosition = sectionStartBeatPosition
      invalidateDrawingLayer()
    }
    harmonyBeatView.apply {
      beatPosition = position
      layoutWidth = elementWidth
      layoutHeight = harmonyViewHeight
      invalidate()
    }
  }


  val Pattern<*>.itemCount get() = ceil(length.toDouble() / subdivisionsPerBeat).toInt()

  override fun getItemCount(): Int = with(viewModel) {
    with(viewModel.paletteViewModel) {
      when (interactionMode) {
        BeatScratchToolbar.InteractionMode.EDIT ->
          harmony?.itemCount
            ?: openedMelody?.itemCount
            ?: 0
        BeatScratchToolbar.InteractionMode.VIEW ->
          paletteViewModel.palette.sections.fold(0) { sum, section ->
            sum + section.harmony.itemCount
          }
      }
    }
  }

  override fun invalidate(beatPosition: Int) {
    boundViewHolders.find { it.adapterPosition == beatPosition }?.apply {
      harmonyBeatView.invalidate()
      melodyBeatViews.applyToEach { invalidateDrawingLayer() }
    }
//    (recyclerView.layoutManager!!.findViewByPosition(beatPosition) as? ViewGroup)?.apply {
//      (0 until childCount).map { getChildAt(it) }.forEach { it.invalidate() }
//    }
  }

  fun notifyTickPositionChanged(
    oldTick: Int,
    newTick: Int,
    oldBeat: Int,
    newBeat: Int
  ) {
    if(oldBeat == newBeat) {
      arrayOf(newBeat)
    } else {
      arrayOf(oldBeat, newBeat)
    }.forEach { beatPosition ->
      boundViewHolders.find { it.adapterPosition == beatPosition }?.apply {
        harmonyBeatView.invalidateDrawingLayerIfPositionChanged(oldTick, newTick)
        melodyBeatViews.applyToEach {
          invalidateDrawingLayerIfPositionChanged(oldTick, newTick)
        }
      }
    }
  }
}