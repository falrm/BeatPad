package com.jonlatane.beatpad.view.melody

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.jonlatane.beatpad.model.Pattern
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import com.jonlatane.beatpad.util.smartrecycler.applyToHolders
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.verbose
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
    get() = viewModel.melodyVerticalScrollView.width * 4
  val minimumElementHeight
    get() = recyclerView.run { dip(100) }
  val minimumRecommendedElementHeightForEditing
    get() = (viewModel.melodyVerticalScrollView.height * 5f/12f).toInt()
  val maximumElementHeight: Int get() = recyclerView.run { dip(maximumBeatHeightDp) }
  val maximumRecommendedElementHeightForOverview
    get() = (viewModel.melodyVerticalScrollView.height * 7f/12f).toInt()


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
          it.element.layoutWidth = field
        }
        if(
          viewModel.layoutType == MelodyViewModel.LayoutType.GRID
          && (recyclerView.layoutManager as? GridLayoutManager)?.spanCount
          != recommendedSpanCount
        ) {
          recyclerView.layoutManager = recommendedGridLayoutManager()
        }
      }
      viewModel.paletteViewModel.harmonyViewModel.beatAdapter.elementWidth = field
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
        recyclerView.applyToHolders<MelodyBeatHolder> {
          it.element.layoutHeight = field
        }
        axis.layoutHeight = field
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

  fun linearLayout() {
    recyclerView.layoutManager = linearLayoutManager
    viewModel.melodyVerticalScrollView.scrollingEnabled = true
  }

  fun gridLayout() {
    recyclerView.layoutManager = recommendedGridLayoutManager()
    viewModel.melodyVerticalScrollView.scrollingEnabled = false
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
        it.element.apply {
          invalidate()
        }
      }
    }

  var notationAlpha: Float = 1f
    set(value) {
      field = value
      recyclerView.applyToHolders<MelodyBeatHolder> {
        it.element.apply {
          invalidate()
        }
      }
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyBeatHolder {
    return with(recyclerView) {
      MelodyBeatHolder(
        viewModel = viewModel,
        element = MelodyBeatView(context, viewModel = viewModel).lparams {
          width = elementWidth
          height = elementHeight
        },
        adapter = this@MelodyBeatAdapter
      )
    }
  }

  override fun onBindViewHolder(holder: MelodyBeatHolder, elementPosition: Int) {
    holder.element.beatPosition = elementPosition
    holder.element.layoutWidth = elementWidth
    holder.element.layoutHeight = elementHeight
    holder.element.invalidate()
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

}