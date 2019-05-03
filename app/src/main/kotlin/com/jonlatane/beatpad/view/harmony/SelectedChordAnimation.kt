package com.jonlatane.beatpad.view.harmony

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.jonlatane.beatpad.output.service.let
import com.jonlatane.beatpad.util.viewHolders
import org.jetbrains.anko.AnkoLogger

interface SelectedChordAnimation: AnkoLogger {
  companion object {
    const val steps: Int = 67
  }
  var isChoosingHarmonyChord: Boolean

  fun HarmonyViewModel.animateBeatsOfSelectedChord() {
    (harmony to selectedHarmonyElements).let { harmony, selectedHarmonyElements ->
      harmonyElementRecycler?.viewHolders<HarmonyBeatHolder>()?.filter {
        val beatViewRange =
           it.element.beatPosition      * harmony.subdivisionsPerBeat..
          (it.element.beatPosition + 1) * harmony.subdivisionsPerBeat
        selectedHarmonyElements.first < beatViewRange.last ||
          selectedHarmonyElements.last > beatViewRange.first
      }?.map { it.element }
        ?.let { views ->
          ValueAnimator.ofInt(0, steps).apply {
            addUpdateListener { valueAnimator ->
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
    }
  }

  fun animateAtStep(beatSelectionAnimationPosition: Int, harmonyBeatViews: Iterable<HarmonyBeatView>) {
    harmonyBeatViews.forEach {
      it.beatSelectionAnimationPosition = beatSelectionAnimationPosition
      it.invalidate()
    }
  }
}