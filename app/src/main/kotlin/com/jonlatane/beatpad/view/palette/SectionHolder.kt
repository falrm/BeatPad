package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import org.jetbrains.anko.*
import java.util.*

class SectionHolder(parent: ViewGroup, val viewModel: PaletteViewModel) : RecyclerView.ViewHolder(
  _LinearLayout(parent.context).apply {
    isClickable = true
    isLongClickable = true
    textView {
      id = R.id.section_name
      textSize = 25f
      layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
      minimumWidth = context.dip(90)
      gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
      typeface = MainApplication.chordTypeface
    }.lparams(wrapContent, wrapContent) {
      setMargins(
        dip(10),
        dip(2),
        dip(10),
        dip(2)
      )
    }
  }
) {
  companion object {
    fun sectionColor(sectionIndex: Int) = arrayOf(
      R.drawable.orbifold_chord_major,
      R.drawable.orbifold_chord_minor,
      R.drawable.orbifold_chord_dominant,
      R.drawable.orbifold_chord_augmented,
      R.drawable.orbifold_chord_diminished
    )[sectionIndex % 5]
  }
  val adapter: SectionListAdapter get() = viewModel.sectionListAdapter!!
  val section: Section?
    get() = when (adapterPosition) {
      viewModel.palette.sections.size -> null
      else                            -> viewModel.palette.sections.getOrNull(adapterPosition)
    }
  val sectionName: TextView by lazy { itemView.findViewById<TextView>(R.id.section_name) }
  val menu: PopupMenu by lazy {
    PopupMenu(parent.context, sectionName).also {
      it.inflate(R.menu.section_menu)
      it.setOnMenuItemClickListener { item ->
        when (item) {
          deleteSection -> {
            if(viewModel.palette.sections.size <= 1) {
              parent.context.toast("Cannot delete the final section!")
            } else {
              showConfirmDialog(
                sectionName.context,
                "Really delete section?",
                "Yes, delete section"
              ) {
                val originalPosition = adapterPosition
                val originalSection = section
                viewModel.palette.sections.removeAt(adapterPosition)
                viewModel.sectionListAdapter?.notifyItemRemoved(adapterPosition)
                viewModel.sectionListAdapter?.notifyItemRangeChanged(
                  adapterPosition,
                  viewModel.palette.sections.size - adapterPosition
                )
                if (originalSection == BeatClockPaletteConsumer.section) {
                  BeatClockPaletteConsumer.section = viewModel.palette.sections.getOrElse(originalPosition - 1) {
                    _ -> viewModel.palette.sections[0]
                  }
                }
              }
            }
          }
          removeHarmony -> {
            section?.harmony = null
            if (BeatClockPaletteConsumer.section == section) {
              viewModel.harmonyViewModel.notifyHarmonyChanged()
              viewModel.melodyViewModel.beatAdapter.notifyDataSetChanged()
            }
            invalidate()
          }
          addHarmony -> {
            section?.harmony = Harmony(
              changes = TreeMap(
                mapOf(
                  0 to viewModel.orbifold.chord
                )
              ),
              length = 64,
              subdivisionsPerBeat = 4
            )
            if (BeatClockPaletteConsumer.section == section) {
              viewModel.harmonyViewModel.notifyHarmonyChanged()
              viewModel.melodyViewModel.beatAdapter.notifyDataSetChanged()
            }
            invalidate()
          }
        }
        true
      }
    }
  }
  private val deleteSection: MenuItem get() = menu.menu.findItem(R.id.deleteSection)
  private val addHarmony: MenuItem get() = menu.menu.findItem(R.id.addHarmonyToSection)
  private val removeHarmony: MenuItem get() = menu.menu.findItem(R.id.removeHarmonyFromSection)

  fun invalidate() {
    deleteSection.isVisible = viewModel.palette.sections.size > 1
    addHarmony.isVisible = false /*when(section) {
      null -> false
      else -> when(section!!.harmony) {
        null -> true
        else -> false
      }
    }*/
    removeHarmony.isVisible = false /*when(section) {
      null -> false
      else -> when(section!!.harmony) {
        null -> false
        else -> true
      }
    }*/
    itemView.backgroundResource = when (section) {
      BeatClockPaletteConsumer.section -> sectionColor(adapterPosition)
      else -> R.drawable.orbifold_chord
    }

    if (adapterPosition < viewModel.palette.sections.size) {
      makeEditableSection()
    } else {
      makeAddButton()
    }
  }


  private fun makeEditableSection() {
    val section = viewModel.palette.sections[adapterPosition]
    sectionName.apply {
      text = section.name
    }
    itemView.apply {
      setOnClickListener {
        BeatClockPaletteConsumer.section = section
      }
      setOnLongClickListener {
        //vibrate(150)
        menu.show()
        true
      }
    }
  }

  private fun makeAddButton() {
    sectionName.apply {
      text = "+"
    }
    itemView.apply {
      setOnClickListener {
        adapter.addSection()
      }
      setOnLongClickListener {
        //vibrate(150)
        adapter.addSection()
        true
      }
    }
  }
}