package com.jonlatane.beatpad.view.melody

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import com.jonlatane.beatpad.util.smartrecycler.applyToHolders
import com.jonlatane.beatpad.view.harmony.HarmonyBeatView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round


class MelodyBeatAdapter(
  val viewModel: MelodyViewModel,
  override val recyclerView: _RecyclerView
) : SmartAdapter<MelodyBeatHolder>(), AnkoLogger, BeatAdapter {
  companion object {
    const val initialBeatWidthDp: Float = 125f
    const val initialBeatHeightDp: Float = 400f
    const val minimumBeatWidthDp: Float = 30f
    const val maximumBeatHeightDp: Float = 10000f
  }

  private val axis get() = viewModel.verticalAxis!!
  val minimumElementWidth get() = recyclerView.run { dip(minimumBeatWidthDp) }
  val maximumElementWidth
    get() = when(viewModel.layoutType) {
      MelodyViewModel.LayoutType.GRID   -> viewModel.melodyVerticalScrollView.width / 2
      MelodyViewModel.LayoutType.LINEAR -> viewModel.melodyVerticalScrollView.width * 4
    }
  val minimumElementHeight get() = when(viewModel.layoutType) {
    MelodyViewModel.LayoutType.LINEAR -> minimumRecommendedElementHeightForEditing
    MelodyViewModel.LayoutType.GRID   -> recyclerView.run { dip(100) }
  }
  val maximumElementHeight: Int get() = when(viewModel.layoutType) {
    MelodyViewModel.LayoutType.LINEAR -> recyclerView.run { dip(maximumBeatHeightDp) }
    MelodyViewModel.LayoutType.GRID   -> maximumRecommendedElementHeightForOverview
  }
  val minimumRecommendedElementHeightForEditing
    get() = when {
      viewModel.paletteViewModel.storageContext.configuration.tablet -> (viewModel.melodyVerticalScrollView.height * 5f / 12f).toInt()
      else -> (viewModel.melodyVerticalScrollView.height * 7f / 12f).toInt()
    }
  val maximumRecommendedElementHeightForOverview
    get() = when {
      viewModel.paletteViewModel.storageContext.configuration.tablet -> (viewModel.melodyVerticalScrollView.height * 7f / 12f).toInt()
      else -> (viewModel.melodyVerticalScrollView.height * 5f / 12f).toInt()
    }


  override var elementWidth: Int = recyclerView.run { dip(initialBeatWidthDp) }
    set(value) {
      if(field != value) {
        field = when {
          value < minimumElementWidth -> {
            minimumElementWidth
          }
          value > maximumElementWidth -> {
            maximumElementWidth
          }
          else -> value
        }
        verbose("Setting width to $field")
        recyclerView.applyToHolders<MelodyBeatHolder> {
          it.melodyBeatView.layoutWidth = field
        }
        if(
          viewModel.layoutType == MelodyViewModel.LayoutType.GRID
          && (recyclerView.layoutManager as? GridLayoutManager)?.spanCount
          != recommendedSpanCount
        ) {
          recyclerView.layoutManager = recommendedGridLayoutManager()
        }
        viewModel.melodyView.syncScrollingChordText()
      }
      //viewModel.paletteViewModel.harmonyViewModel.beatAdapter.elementWidth = field
    }

  var elementHeight = recyclerView.run { dip(initialBeatHeightDp) }
    set(value) {
      if(field != value) {
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
            it.melodyBeatView.forceLayout()
            it.element.requestLayout()
            it.element.forceLayout() //TODO: This helps
          }
          recyclerView.requestLayout() //TODO: This helps
        }
        recyclerView.applyToHolders<MelodyBeatHolder> {
          it.harmonyBeatView.layoutHeight = recyclerView.harmonyViewHeight
          it.melodyBeatView.layoutHeight = field
        }
        sendStuff()
        recyclerView.post { sendStuff() }
        //recyclerView.adapter.notifyDataSetChanged()
        //recyclerView.adapter.notifyItemRangeChanged(0, recyclerView.adapter.itemCount)
        axis.topMargin = recyclerView.harmonyViewHeight
        axis.layoutHeight = field
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
    melodyView.rightSpacer.animateWidth(0)
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
    melodyView.rightSpacer.animateWidth(viewModel.melodyView.dip(5)) {
      onZoomFinished()
    }
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
        it.melodyBeatView.apply {
          invalidate()
        }
      }
    }

  var notationAlpha: Float = 1f
    set(value) {
      field = value
      recyclerView.applyToHolders<MelodyBeatHolder> {
        it.melodyBeatView.apply {
          invalidate()
        }
      }
    }

  val RecyclerView.harmonyViewHeight     get() = min(dip(45),elementHeight/10)
  val MelodyBeatHolder.harmonyViewHeight get() = min(harmonyBeatView.dip(45),elementHeight/10)
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyBeatHolder {
    return with(recyclerView) {
      val melodyBeatView = MelodyBeatView(context, viewModel = viewModel)
        .lparams(elementWidth, elementHeight)
      val harmonyBeatView = HarmonyBeatView(
        context,
        viewModel = viewModel.paletteViewModel.harmonyViewModel
      ).lparams(elementWidth, harmonyViewHeight)
      val element: View = _LinearLayout(
        melodyBeatView.context
      ).apply {
        orientation = LinearLayout.VERTICAL
        addView(harmonyBeatView)
        addView(melodyBeatView)
      }.lparams(wrapContent, wrapContent)
      MelodyBeatHolder(
        viewModel = viewModel,
        harmonyBeatView = harmonyBeatView,
        melodyBeatView = melodyBeatView,
        element = element,
        adapter = this@MelodyBeatAdapter
      )
    }
  }

  override fun onBindViewHolder(holder: MelodyBeatHolder, position: Int) = with(holder) {
//    element.layoutWidth = elementWidth
//    element.layoutHeight = elementHeight + harmonyViewHeight
    melodyBeatView.beatPosition = position
    melodyBeatView.layoutWidth = elementWidth
    melodyBeatView.layoutHeight = elementHeight
    melodyBeatView.invalidate()
    harmonyBeatView.beatPosition = position
    harmonyBeatView.layoutWidth = elementWidth
    harmonyBeatView.layoutHeight = harmonyViewHeight
    harmonyBeatView.invalidate()
  }


  val Pattern<*>.itemCount get() = ceil(length.toDouble()/subdivisionsPerBeat).toInt()

  override fun getItemCount(): Int = viewModel.run {
    when(sectionLayoutType) {
      MelodyViewModel.SectionLayoutType.SINGLE_SECTION ->
        harmony?.itemCount
          ?: openedMelody?.itemCount
          ?: 0
      MelodyViewModel.SectionLayoutType.FULL_PALETTE ->
        paletteViewModel.palette.sections.fold(0) { sum, section ->
          sum + section.harmony.itemCount
        }
    }
  }

  override fun invalidate(beatPosition: Int) {
    boundViewHolders.find { it.adapterPosition == beatPosition }?.apply {
      harmonyBeatView.invalidate()
      melodyBeatView.invalidate()
    }
    (recyclerView.layoutManager.findViewByPosition(beatPosition) as? ViewGroup)?.apply {
      (0 until childCount).map { getChildAt(it) }.forEach { it.invalidate() }
    }
  }

}