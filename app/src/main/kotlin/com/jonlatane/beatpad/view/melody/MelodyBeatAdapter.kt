package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.util.applyToHolders
import com.jonlatane.beatpad.util.layoutHeight
import com.jonlatane.beatpad.util.layoutWidth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.info
import org.jetbrains.anko.recyclerview.v7._RecyclerView


class MelodyBeatAdapter(
  val viewModel: MelodyViewModel,
  val recyclerView: _RecyclerView
) : RecyclerView.Adapter<MelodyBeatHolder>(), AnkoLogger {
  companion object {
    const val initialBeatWidthDp: Float = 150f
    const val initialBeatHeightDp: Float = 1000f
    const val minimumBeatWidthDp: Float = 30f
    const val maximumBeatHeightDp: Float = 2500f
  }

  private val axis get() = viewModel.verticalAxis!!
  private val minimumElementWidth = recyclerView.run { dip(minimumBeatWidthDp) }
  private val maximumElementWidth get() = viewModel.melodyCenterVerticalScroller.width / 2
  private val minimumElementHeight get() = viewModel.melodyCenterVerticalScroller.height
  private val maximumElementHeight get() = recyclerView.run { dip(maximumBeatHeightDp) }

  @Volatile
  var elementWidth = recyclerView.run { dip(initialBeatWidthDp) }
    @Synchronized set(value) {
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
        info("Setting width to $field")
        recyclerView.applyToHolders<MelodyBeatHolder> {
          it.element.layoutWidth = field
        }
        viewModel.paletteViewModel.harmonyViewModel.beatAdapter.elementWidth = field
      }
    }

  @Volatile
  var elementHeight = recyclerView.run { dip(initialBeatHeightDp) }
    @Synchronized set(value) {
      field = when {
        value < minimumElementHeight -> {
          minimumElementHeight
        }
        value > maximumElementHeight -> {
          maximumElementHeight
        }
        else -> value
      }

      info("Setting height to $field")
      recyclerView.applyToHolders<MelodyBeatHolder> {
        it.element.layoutHeight = field
      }
      axis.layoutHeight = field
    }


  fun invalidate(beatPosition: Int) {
    recyclerView.layoutManager.findViewByPosition(beatPosition)?.invalidate()
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