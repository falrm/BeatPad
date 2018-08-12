package com.jonlatane.beatpad.view.melody

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.applyToHolders
import com.jonlatane.beatpad.util.layoutHeight
import com.jonlatane.beatpad.util.layoutWidth
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView


class MelodyElementAdapter(
  val viewModel: MelodyViewModel,
  val recyclerView: _RecyclerView
) : RecyclerView.Adapter<MelodyElementHolder>(), AnkoLogger {
  private val axis get() = viewModel.verticalAxis!!
  private val minimumElementWidth = recyclerView.run { dip(27f) }
  private val maximumElementWidth get() = viewModel.melodyCenterVerticalScroller.width / 2
  private val minimumElementHeight get() = viewModel.melodyCenterVerticalScroller.height
  private val maximumElementHeight get() = viewModel.melodyCenterVerticalScroller.height * 3

  @Volatile
  var elementWidth = recyclerView.run { dimen(R.dimen.subdivision_controller_size) }
    @Synchronized set(value) {
      field = when {
        value > minimumElementWidth -> {
          value
        }
        else -> minimumElementWidth
      }
      info("Setting width to $field")
      recyclerView.applyToHolders<MelodyElementHolder> {
        it.element.layoutWidth = field
      }
      (viewModel as? PaletteViewModel)?.harmonyViewModel?.chordAdapter?.elementWidth = field
    }

  @Volatile
  var elementHeight = recyclerView.run { dip(1000f) }
    @Synchronized set(value) {
      field = when {
        value > minimumElementHeight -> {
          value
        }
        else -> minimumElementHeight
      }

      info("Setting height to $field")
      recyclerView.applyToHolders<MelodyElementHolder> {
        it.element.layoutHeight = field
      }
      axis.layoutHeight = field
    }


  fun invalidate(position: Int) {
    recyclerView.layoutManager.findViewByPosition(position)?.invalidate()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyElementHolder {
    return with(recyclerView) {
      MelodyElementHolder(
        viewModel = viewModel,
        element = MelodyElementView(context).apply {
          viewModel = this@MelodyElementAdapter.viewModel
        }.lparams {
          width = elementWidth
          height = elementHeight
        },
        adapter = this@MelodyElementAdapter
      )
    }
  }

  override fun onBindViewHolder(holder: MelodyElementHolder, elementPosition: Int) {
    holder.element.elementPosition = elementPosition
    holder.element.layoutWidth = elementWidth
    holder.element.layoutHeight = elementHeight
    holder.element.invalidate()
  }

  override fun getItemCount(): Int = viewModel.openedMelody.elements.size
}