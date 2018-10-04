package com.jonlatane.beatpad.view.harmony

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.service.let
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import com.jonlatane.beatpad.view.zoomableRecyclerView
import kotlinx.io.pool.DefaultPool
import org.jetbrains.anko.*
import java.util.*

class HarmonyView(
  context: Context,
  val viewModel: PaletteViewModel,
  init: HideableRelativeLayout.() -> Unit = {}
) : HideableRelativeLayout(context), AnkoLogger {
  companion object {
  }
  // We will render these at the top level on scroll
  val chordChangeLabels: MutableMap<Int, TextView> = mutableMapOf()
  private val chordChangeLabelPool: DefaultPool<TextView> = object : DefaultPool<TextView>(16) {
    override fun produceInstance() = textView {
      textSize = 20f
      maxLines = 1
      singleLine = true
      ellipsize = TextUtils.TruncateAt.END
      typeface = MainApplication.chordTypeface
      elevation = 5f
    }.lparams(width = wrapContent, height = wrapContent) {
      topMargin = dip(10)
      marginStart = dip(40)
      alignParentLeft()
      alignParentTop()
    }

    override fun validateInstance(instance: TextView) {
      super.validateInstance(instance)
      instance.apply {
        info("Clearing textview $text")
        text = ""
        translationX = 0f
        alpha = 1f
      }
    }
  }

  init {
    viewModel.harmonyView = this
    backgroundColor = color(R.color.colorPrimaryDark)
    val textView1 = chordChangeLabelPool.borrow().apply {
      id = View.generateViewId()
    }
    viewModel.harmonyViewModel.harmonyElementRecycler = zoomableRecyclerView {
      id = R.id.center_h_scroller
      isFocusableInTouchMode = true
      elevation = 3f
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
        isItemPrefetchEnabled = false
      }
      overScrollMode = View.OVER_SCROLL_NEVER


      zoomHandler = { xDelta, yDelta ->
        AnkoLogger<MelodyViewModel>().info("Zooming: xDelta=$xDelta, yDelta=$yDelta")
        when {
          (xDelta.toInt() != 0) -> {
            viewModel.harmonyViewModel.beatAdapter.apply {
              elementWidth += xDelta.toInt()
              notifyDataSetChanged()
            }
            true
          }
          else -> false
        }
      }
      overScrollMode = View.OVER_SCROLL_NEVER
      viewModel.harmonyViewModel.beatAdapter = HarmonyBeatAdapter(viewModel, this)
      adapter = viewModel.harmonyViewModel.beatAdapter
      /*adapter.registerAdapterDataObserver(
        object : RecyclerView.AdapterDataObserver() {
          override fun onItemRangeInserted(start: Int, count: Int) {
            //updateEmptyViewVisibility(this@recyclerView)
          }

          override fun onItemRangeRemoved(start: Int, count: Int) {
            //updateEmptyViewVisibility(this@recyclerView)
          }
        })*/
    }.lparams(0, 0)


    post {
      viewModel.harmonyViewModel.harmonyElementRecycler?.lparams {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = textView1.height + dip(10f)
        topMargin = dip(5)
        marginStart = dip(30)
        alignParentRight()
        alignParentTop()
      }
    }
  }

  fun syncScrollingChordText() {
    val (firstBeatPosition, lastBeatPosition) = (viewModel.harmonyViewModel
      .harmonyElementRecycler?.layoutManager as? LinearLayoutManager)?.run {
      findFirstVisibleItemPosition() to findLastVisibleItemPosition()
    } ?: 0 to 0
    // Render the harmony if there is one
    val harmony: Harmony? = viewModel.harmonyViewModel.harmony
    if (harmony != null) {
      val changes = harmony.changes
      val subdivisionsPerBeat = harmony.subdivisionsPerBeat
      val upperBound = (lastBeatPosition + 1) * subdivisionsPerBeat
      val strictLowerBound = subdivisionsPerBeat * firstBeatPosition
      val lowerBound = Math.min(strictLowerBound, harmony.lowerKey(strictLowerBound))

      val visibleChanges: SortedMap<Int, Chord> = harmony.changes
          .headMap(upperBound, true)
          .tailMap(lowerBound)
      verbose { "Visible changes for beats [$firstBeatPosition, $lastBeatPosition]/[$lowerBound, $upperBound]: $visibleChanges" }


      val locationOnScreen = intArrayOf(-1, -1)
      val recyclerView = viewModel.harmonyViewModel.harmonyElementRecycler!!

      var lastView: TextView? = null
      var lastTranslationX: Float? = null
      visibleChanges.forEach { (position, chord) ->
        val label = chordChangeLabels[position]
          ?: chordChangeLabelPool.borrow().also { chordChangeLabels[position] = it }
        label.apply textView@{
          val beatPosition = (position.toFloat() / subdivisionsPerBeat).toInt()
          (recyclerView.layoutManager as LinearLayoutManager)
            .run {
              findViewByPosition(beatPosition) ?:
              if(beatPosition < firstBeatPosition)findViewByPosition(firstBeatPosition)
              else null
            }?.let { view: View ->
              view.getLocationOnScreen(locationOnScreen)
              val beatViewX = locationOnScreen[0]
              val chordPositionOffset = view.width * (position % subdivisionsPerBeat).toFloat() / subdivisionsPerBeat
              val chordPositionX = beatViewX + chordPositionOffset
              recyclerView.getLocationOnScreen(locationOnScreen)
              val recyclerViewX = locationOnScreen[0]
              val chordTranslationX = Math.max(0f, (chordPositionX - recyclerViewX).toFloat())

              verbose { "Location of view for $text @ (beat $beatPosition) is $chordTranslationX" }
              this@textView.translationX = chordTranslationX
              this@textView.text = chord.name
              this@textView.alpha = 1f
              this@textView.layoutParams = this@textView.layoutParams.apply {
                maxWidth = (view.width * 1.5f).toInt()
              }

              (lastTranslationX to lastView).let { lastTranslationX, lastView ->
                if(chordTranslationX - lastTranslationX <= lastView.width) {
                  lastView.alpha = (chordTranslationX - lastTranslationX) / lastView.width
                }
              }
              lastView = this@textView
              lastTranslationX = chordTranslationX
            }
        }
      }
      val entriesToRemove = chordChangeLabels.filterKeys { !visibleChanges.containsKey(it) }
      entriesToRemove.forEach { (key, textView) ->
        chordChangeLabels.remove(key)
        chordChangeLabelPool.recycle(textView)
      }
    } else {
      //No harmony, render some placeholder stuff
      chordChangeLabels.toMap().forEach { (key, textView) ->
        chordChangeLabels.remove(key)
        chordChangeLabelPool.recycle(textView)
      }
      chordChangeLabels[-1] = chordChangeLabelPool.borrow().apply {
        text = "No harmony"
        alpha = 1f
        layoutParams = this.layoutParams.apply {
          maxWidth = Int.MAX_VALUE
        }
      }
    }
  }
}
