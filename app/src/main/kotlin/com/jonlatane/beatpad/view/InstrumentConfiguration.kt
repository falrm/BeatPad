package com.jonlatane.beatpad.view

import BeatClockPaletteConsumer.currentSectionDrawable
import android.support.constraint.ConstraintSet
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View.generateViewId
import android.view.ViewManager
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.vibrate
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.sdk25.coroutines.onClick

val input = listOf<Pair<String, Int>>().also { input ->
  input.groupBy { it.first }
    .maxBy { entry -> entry.value.sumBy { it.second } }
    ?.key
}
interface InstrumentConfiguration: BaseConfiguration {
  fun ViewManager.instrumentPartPicker(
    availableParts: List<Part>,
    getSelectedPart: () -> Part,
    setSelectedPart: (Part) -> Unit
  ) = constraintLayout {
    id = generateViewId()
    lateinit var adapter: RecyclerView.Adapter<*>
    val initialPosition = try { availableParts.indexOf(getSelectedPart()) } catch(t: Throwable) {0}
    val label = textView("Part") {
      id = generateViewId()
      typeface = MainApplication.chordTypefaceBold
      textSize = 16f
      //gravity = Gravity.START or Gravity.CENTER_VERTICAL
    }.lparams(wrapContent, wrapContent)
    val recycler = InstaRecycler.instaRecycler(
      context,
      factory = { nonDelayedRecyclerView().apply { id = generateViewId() } },
      layoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.HORIZONTAL,
        false
      ).apply {
        isItemPrefetchEnabled = false
      },

      holderViewFactory = {
        TextView(context)
          .apply {
            id = InstaRecycler.example_id
            textSize = 16f
            padding = dip(15f)
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, wrapContent)
      },
      itemCount = { availableParts.count() },
      binder = { position ->
        findViewById<TextView>(InstaRecycler.example_id).apply {
          val part = availableParts[position]
          text = part.instrument.instrumentName
          backgroundResource = when {
            getSelectedPart() == part -> currentSectionDrawable
            (part.instrument as? MIDIInstrument)?.drumTrack == true -> R.drawable.part_background_drum
            else -> R.drawable.part_background
          }
          padding = dip(16)
          isClickable = true
          onClick {
            vibrate(10)
            setSelectedPart(part)
            adapter.notifyDataSetChanged()
          }
        }
      }
    )
    adapter = recycler.adapter
    recycler.scrollToPosition(initialPosition)

    applyConstraintSet {
      label {
        connect(
          ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.TOP of ConstraintSet.PARENT_ID,// margin dip(15),
          ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID margin dip(15)//,
          //ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID margin dip(15)
        )
      }
      recycler {
        connect(
          ConstraintSetBuilder.Side.TOP to ConstraintSetBuilder.Side.BOTTOM of label margin dip(15),
          ConstraintSetBuilder.Side.START to ConstraintSetBuilder.Side.START of ConstraintSet.PARENT_ID margin dip(15),
          ConstraintSetBuilder.Side.END to ConstraintSetBuilder.Side.END of ConstraintSet.PARENT_ID margin dip(15),
          ConstraintSetBuilder.Side.BOTTOM to ConstraintSetBuilder.Side.BOTTOM of ConstraintSet.PARENT_ID margin dip(15)
        )
      }
    }
  }
}