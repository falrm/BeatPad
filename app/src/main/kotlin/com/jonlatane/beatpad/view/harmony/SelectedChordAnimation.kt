package com.jonlatane.beatpad.view.harmony

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.jonlatane.beatpad.util.smartrecycler.viewHolders
import com.jonlatane.beatpad.view.melody.MelodyBeatHolder
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
        val views: List<HarmonyBeatView> = (harmony to selectedHarmonyElements).letCheckNull {
          harmony, selectedHarmonyElements ->
          val fromHarmonyView = harmonyElementRecycler?.viewHolders<HarmonyBeatHolder>()?.filter {
            val beatViewRange =
              it.element.beatPosition * harmony.subdivisionsPerBeat..
                (it.element.beatPosition + 1) * harmony.subdivisionsPerBeat
            selectedHarmonyElements.first < beatViewRange.last ||
              selectedHarmonyElements.last > beatViewRange.first
          }?.map { it.element }
          val fromMelodyView = paletteViewModel?.melodyViewModel?.melodyRecyclerView?.viewHolders<MelodyBeatHolder>()?.filter {
            val beatViewRange =
              it.harmonyBeatView.beatPosition * harmony.subdivisionsPerBeat..
                (it.harmonyBeatView.beatPosition + 1) * harmony.subdivisionsPerBeat
            selectedHarmonyElements.first < beatViewRange.last ||
              selectedHarmonyElements.last > beatViewRange.first
          }?.map { it.harmonyBeatView }
          (fromHarmonyView ?: emptyList()) + (fromMelodyView ?: emptyList())
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
      duration = 9600L // 5fps (12 "frames") over 2400MS
    }.start()
  }

  fun animateAtStep(beatSelectionAnimationPosition: Int, harmonyBeatViews: Iterable<HarmonyBeatView>) {
    harmonyBeatViews.forEach {
      it.beatSelectionAnimationPosition = beatSelectionAnimationPosition
      it.invalidate()
    }
  }
}