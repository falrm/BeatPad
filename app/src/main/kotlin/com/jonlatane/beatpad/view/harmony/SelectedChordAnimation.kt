package com.jonlatane.beatpad.view.harmony

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.jonlatane.beatpad.util.viewHolders
import io.multifunctions.letCheckNull
import org.jetbrains.anko.AnkoLogger

interface SelectedChordAnimation: AnkoLogger {
  companion object {
    const val steps: Int = 67
  }
  var isChoosingHarmonyChord: Boolean

  fun HarmonyViewModel.animateBeatsOfSelectedChord() {

    ValueAnimator.ofInt(0, steps).apply {
      addUpdateListener { valueAnimator ->
        val views = (harmony to selectedHarmonyElements).letCheckNull { harmony, selectedHarmonyElements ->
          harmonyElementRecycler?.viewHolders<HarmonyBeatHolder>()?.filter {
            val beatViewRange =
              it.element.beatPosition * harmony.subdivisionsPerBeat..
                (it.element.beatPosition + 1) * harmony.subdivisionsPerBeat
            selectedHarmonyElements.first < beatViewRange.last ||
              selectedHarmonyElements.last > beatViewRange.first
          }?.map { it.element }
        } ?: emptyList()
        animateAtStep(valueAnimator.animatedValue as Int, views)
      }
      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          if(isChoosingHarmonyChord) {
            animateBeatsOfSelectedChord()
          }
        }
      })
      interpolator = LinearInterpolator()
      duration = 4800L // 5fps (12 "frames") over 2400MS
    }.start()
  }

  fun animateAtStep(beatSelectionAnimationPosition: Int, harmonyBeatViews: Iterable<HarmonyBeatView>) {
    harmonyBeatViews.forEach {
      it.beatSelectionAnimationPosition = beatSelectionAnimationPosition
      it.invalidate()
    }
  }
}