package com.jonlatane.beatpad.view.melody

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.jonlatane.beatpad.util.applyToHolders
import com.jonlatane.beatpad.util.defaultDuration
import com.jonlatane.beatpad.util.layoutHeight
import com.jonlatane.beatpad.util.layoutWidth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.recyclerview.v7._RecyclerView
import org.jetbrains.anko.verbose
import kotlin.math.round


class MelodyBeatAdapter(
  val viewModel: MelodyViewModel,
  override val recyclerView: _RecyclerView
) : RecyclerView.Adapter<MelodyBeatHolder>(), AnkoLogger, BeatAdapter {
  companion object {
    const val initialBeatWidthDp: Float = 125f
    const val initialBeatHeightDp: Float = 400f
    const val minimumBeatWidthDp: Float = 30f
    const val maximumBeatHeightDp: Float = 2500f
  }

  private val axis get() = viewModel.verticalAxis!!
  private val minimumElementWidth
    get() = recyclerView.run { dip(minimumBeatWidthDp) }
  private val maximumElementWidth
    get() = viewModel.melodyVerticalScrollView.width / 2
  private val minimumElementHeight
    get() = recyclerView.run { dip(100) }
  private val maximumElementHeight
    get() = recyclerView.run { dip(maximumBeatHeightDp) }


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
          useGridLayoutManager
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

        if(
          !useGridLayoutManager
          && recyclerView.layoutManager is GridLayoutManager
        ) {
          recyclerView.layoutManager = linearLayoutManager
          viewModel.melodyVerticalScrollView.scrollingEnabled = true
        } else if(
          useGridLayoutManager
          && (recyclerView.layoutManager as? GridLayoutManager)?.spanCount
              != recommendedSpanCount
        ) {
          recyclerView.layoutManager = recommendedGridLayoutManager()
          viewModel.melodyVerticalScrollView.scrollingEnabled = false
        }
      }
    }
  val recommendedSpanCount: Int
    get() = round(viewModel.melodyVerticalScrollView.width.toFloat() / elementWidth).toInt()
  val useGridLayoutManager: Boolean
    get() = elementHeight <= (viewModel.melodyVerticalScrollView.height * 2f / 3f).toInt()
  val linearLayoutManager = LinearLayoutManager(
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

  fun animateElementHeight(height: Int, duration: Long = defaultDuration, endAction: (() -> Unit)? = null) {
      val anim = ValueAnimator.ofInt(this.elementHeight, height)
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

  override fun getItemCount(): Int = viewModel.openedMelody?.let { melody ->
    Math.ceil(melody.length.toDouble() / melody.subdivisionsPerBeat).toInt()
  }?: 1 // Always render at least one item, for layout sanity

}