package com.jonlatane.beatpad.view.harmony

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.util.leftMargin
import com.jonlatane.beatpad.view.palette.BeatScratchToolbar
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import io.multifunctions.letCheckNull
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.*
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

interface ChordTextPositioner: AnkoLogger {
  companion object: AnkoLogger {
    val harmonyFindingVector = Vector<Harmony>(16)
  }
  val viewModel: PaletteViewModel
  val marginForKey: Int
  /** Vector of beat hashes (based on BCPC.ticksPerBeat as a max subdivisionPerBeat for chord change labels*/
  val chordChangeLabels: MutableMap<Int, TextView>
  val chordChangeLabelPool: DefaultPool<TextView>
  val recycler: RecyclerView
  val supportGridLayout: Boolean get() = false
  val defaultChordChangeLabelPool get() = with(this as _RelativeLayout) {
    object : DefaultPool<TextView>(16) {
      override fun produceInstance() = textView {
        textSize = 20f
        textScaleX = 0.9f
        maxLines = 1
        singleLine = true
        //ellipsize = TextUtils.TruncateAt.END
        isHorizontalFadingEdgeEnabled = true
        typeface = MainApplication.chordTypefaceBold
        elevation = 5f
      }.lparams(width = wrapContent, height = wrapContent) {
        alignParentLeft()
        if(supportGridLayout) {
          below(viewModel.melodyViewModel.melodyEditingToolbar)
        } else {
          centerVertically()
        }
        marginStart = marginForKey
      }

      override fun validateInstance(instance: TextView) {
        super.validateInstance(instance)
        instance.apply {
          verbose { "Clearing textview $text" }
          text = ""
          translationX = 0f
          alpha = 1f
        }
      }
    }
  }

  fun syncScrollingChordText(postAgainAfter: Boolean = true) {
    val (firstBeatPosition, lastBeatPosition) = (recycler.layoutManager as LinearLayoutManager).run {
      findFirstVisibleItemPosition() to findLastVisibleItemPosition()
    }
    var renderedAHarmony = false
    // Render the harmony if there is one
    when(viewModel.interactionMode) {
      BeatScratchToolbar.InteractionMode.EDIT -> {
        viewModel.harmonyViewModel.harmony?.let { harmony ->

          //
          renderedAHarmony = true
          positionChordTextOntoView(harmony, 0, harmony.lengthInBeats - 1, firstBeatPosition, lastBeatPosition)
        }
      }
      BeatScratchToolbar.InteractionMode.VIEW -> {
        var sectionStartBeat = 0
        var sectionEndBeat = 0
        loop@ for(section in viewModel.palette.sections) {
          val harmony = section.harmony
          sectionEndBeat = sectionStartBeat + harmony.lengthInBeats
          if(sectionStartBeat <= lastBeatPosition || sectionEndBeat >= firstBeatPosition) {
            renderedAHarmony = true
            positionChordTextOntoView(harmony, sectionStartBeat, sectionEndBeat, firstBeatPosition, lastBeatPosition)
          }
          sectionStartBeat = sectionEndBeat
        }
      }
    }
//    if(!renderedAHarmony) {
//      //No harmony, render some placeholder stuff
//      chordChangeLabels.toMap().forEach { (key, textView) ->
//        chordChangeLabels.remove(key)
//        chordChangeLabelPool.recycle(textView)
//      }
//      chordChangeLabels[-1] = chordChangeLabelPool.borrow().apply {
//        text = "No harmony"
//        alpha = 1f
//        layoutParams = this.layoutParams.apply {
//          maxWidth = Int.MAX_VALUE
//        }
//      }
//    }
    harmonyFindingVector.clear()
    if(postAgainAfter) {
      recycler.post { syncScrollingChordText(postAgainAfter = false) }
    }
  }

  fun positionChordTextOntoView(
    harmony: Harmony,
    firstHarmonyBeatPosition: Int,
    lastHarmonyBeatPosition: Int,
    firstRenderedBeatPosition: Int,
    lastRenderedBeatPosition: Int
  ) {
    // Derive a submap of harmony.changes that contains visible changes
    val subdivisionsPerBeat = harmony.subdivisionsPerBeat
    val upperBound = (lastRenderedBeatPosition - firstHarmonyBeatPosition + 1) * subdivisionsPerBeat
    val strictLowerBound = subdivisionsPerBeat * (firstRenderedBeatPosition - firstHarmonyBeatPosition)
    val lowerBound = min(strictLowerBound, harmony.lowerKey(strictLowerBound))

    val visibleChanges: SortedMap<Int, Chord> = harmony.changes
      .headMap(upperBound, true)
      .tailMap(lowerBound)
    verbose { "Visible changes for beats [$firstRenderedBeatPosition, $lastRenderedBeatPosition]/[$lowerBound, $upperBound]: $visibleChanges" }


    val locationOnScreen = intArrayOf(-1, -1)

    var lastView: TextView? = null
    var lastTranslationX: Float? = null
    visibleChanges.forEach { (position, chord) ->
      val beatPosition: Float = firstHarmonyBeatPosition + (position.toFloat() / subdivisionsPerBeat)
      val beatHash = firstHarmonyBeatPosition * BeatClockPaletteConsumer.ticksPerBeat + position
      val label = chordChangeLabels[beatHash]
        ?: chordChangeLabelPool.borrow().also { chordChangeLabels[beatHash] = it }
      label.apply textView@{
        (recycler.layoutManager as LinearLayoutManager)
          .run {
            findViewByPosition(floor(beatPosition).toInt())?.let { it to false } ?:
            if(beatPosition < firstRenderedBeatPosition)findViewByPosition(firstRenderedBeatPosition)!! to true
            else null
          }?.let { (beatView: View, isFakeFirstBeat) ->
            beatView.getLocationOnScreen(locationOnScreen)
            val chordPositionX = if(isFakeFirstBeat) {
              0f
            } else {
              val beatViewX = locationOnScreen[0]
              val chordPositionOffset = beatView.width * (position % subdivisionsPerBeat).toFloat() / subdivisionsPerBeat
              beatViewX + chordPositionOffset
            }
            val chordPositionY = locationOnScreen[1]
            recycler.getLocationOnScreen(locationOnScreen)
            val recyclerViewX = locationOnScreen[0]
            val chordTranslationX = max(0f, chordPositionX - recyclerViewX)

            verbose { "Location of view for $text @ (beat $beatPosition) is $chordTranslationX" }
            this@textView.translationX = chordTranslationX
            if(supportGridLayout) {
              val recyclerViewY = locationOnScreen[1]
              val chordTranslationY = chordPositionY.toFloat() - recyclerViewY
              this@textView.translationY = chordTranslationY
              val harmonyViewHeight = with(viewModel.melodyBeatAdapter) { recycler.harmonyViewHeight }
              this@textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,harmonyViewHeight * 0.7f)
              this@textView.leftMargin = marginForKey + round(harmonyViewHeight * 0.2f).toInt()
            }
            this@textView.text = chord.name
            this@textView.alpha = 1f
            this@textView.layoutParams = this@textView.layoutParams.apply {
              val length = (harmony.changes.higherKey(position) ?: harmony.length - 1) - position
              maxWidth = (beatView.width * 0.85f * length.toFloat() / harmony.subdivisionsPerBeat).toInt()
            }

            if(recycler.layoutManager !is GridLayoutManager) {
              (lastTranslationX to lastView).letCheckNull { lastTranslationX, lastView ->
                if (chordTranslationX - lastTranslationX <= lastView.width) {
                  val newAlpha = ((chordTranslationX - lastTranslationX) / lastView.width)
                    .takeIf { it.isFinite() } ?: 0f
                  verbose { "Setting alpha of ${lastView.text} to $newAlpha" }
                  lastView.alpha = newAlpha
                }
              }
              lastView = this@textView
              lastTranslationX = chordTranslationX
            }
          }
      }
    }
    val entriesToRemove = chordChangeLabels.filterKeys {
      it in (firstHarmonyBeatPosition * BeatClockPaletteConsumer.ticksPerBeat)..(lastHarmonyBeatPosition * BeatClockPaletteConsumer.ticksPerBeat)
        && !visibleChanges.containsKey(it - firstHarmonyBeatPosition * BeatClockPaletteConsumer.ticksPerBeat)
    }
    entriesToRemove.forEach { (key, textView) ->
      chordChangeLabels.remove(key)
      chordChangeLabelPool.recycle(textView)
    }
  }
}