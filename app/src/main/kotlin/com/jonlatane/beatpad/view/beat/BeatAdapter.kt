package com.jonlatane.beatpad.view.beat

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.jonlatane.beatpad.util.smartrecycler.applyToHolders
import com.jonlatane.beatpad.util.layoutHeight
import com.jonlatane.beatpad.util.layoutWidth
import com.jonlatane.beatpad.view.melody.MelodyBeatAdapter
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7._RecyclerView


class BeatAdapter<ViewModelType, ViewType>
(
  var viewModel: ViewModelType,
  val recyclerView: _RecyclerView,
  inline val viewGenerator: BeatAdapter<ViewModelType, ViewType>.() -> ViewType,
  inline val lengthGetter: BeatAdapter<ViewModelType, ViewType>.() -> Int?
) : RecyclerView.Adapter<BeatHolder<ViewType>>(), AnkoLogger
  where ViewType: BeatView,
        ViewType: View,
        ViewModelType: BeatViewModel{
  private val axis get() = viewModel.axis
  private val minimumElementWidth = recyclerView.run { dip(MelodyBeatAdapter.minimumBeatWidthDp) }
  private val maximumElementWidth get() = viewModel.beatScrollingArea.width / 2
  private val minimumElementHeight get() = viewModel.beatScrollingArea.height
  private val maximumElementHeight get() = viewModel.beatScrollingArea.height * 3

  @Volatile
  var elementWidth = recyclerView.run { dip(MelodyBeatAdapter.initialBeatWidthDp) }
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
        recyclerView.applyToHolders<BeatHolder<ViewType>> {
          it.element.layoutWidth = field
        }
        (viewModel as? PaletteViewModel)?.harmonyViewModel?.beatAdapter?.elementWidth = field
      }
    }

  @Volatile
  var elementHeight = recyclerView.run { dip(1000f) }
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
      recyclerView.applyToHolders<BeatHolder<ViewType>> {
        it.element.layoutHeight = field
      }
      axis?.layoutHeight = field
    }


  fun invalidate(position: Int) {
    recyclerView.layoutManager!!.findViewByPosition(position)?.invalidate()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeatHolder<ViewType> {
    return with(recyclerView) {
      BeatHolder(
        viewModel = viewModel,
        element = viewGenerator().apply {
          viewModel = this@BeatAdapter.viewModel
        }.lparams {
          width = elementWidth
          height = elementHeight
        },
        adapter = this@BeatAdapter
      )
    }
  }

  override fun onBindViewHolder(holder: BeatHolder<ViewType>, elementPosition: Int) {
    holder.element.beatPosition = elementPosition
    holder.element.layoutWidth = elementWidth
    holder.element.layoutHeight = elementHeight
    holder.element.invalidate()
  }

  override fun getItemCount(): Int = lengthGetter() ?: 0
}