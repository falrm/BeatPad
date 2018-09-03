package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.util.vibrate
import org.jetbrains.anko.*

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
  val adapter: SectionListAdapter get() = viewModel.sectionListAdapter!!
  val section: Section?
    get() = when (adapterPosition) {
      viewModel.palette.sections.size -> null
      else                            -> viewModel.palette.sections[adapterPosition]
    }
  val sectionName: TextView by lazy { itemView.findViewById<TextView>(R.id.section_name) }
  val menu: PopupMenu by lazy {
    PopupMenu(parent.context, sectionName).also {
      it.inflate(R.menu.section_menu)
      it.setOnMenuItemClickListener { item ->
        when (item) {
          deleteSection -> {
            showConfirmDialog(
              sectionName.context,
              "Really delete section?",
              "Yes, delete section"
            ) {
              viewModel.palette.sections.removeAt(adapterPosition)
              viewModel.sectionListAdapter?.notifyItemRemoved(adapterPosition)
              viewModel.sectionListAdapter?.notifyItemRangeChanged(
                adapterPosition,
                viewModel.palette.sections.size - adapterPosition
              )
            }
          }
          removeHarmony -> {
            section?.harmony = null
            if (BeatClockPaletteConsumer.section == section) {
              viewModel.harmonyViewModel.chordAdapter?.notifyDataSetChanged()
            }
            invalidate()
          }
          addHarmony -> {
            section?.harmony = PaletteStorage.baseHarmony
            if (BeatClockPaletteConsumer.section == section) {
              viewModel.harmonyViewModel.chordAdapter?.notifyDataSetChanged()
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
    addHarmony.isVisible = when(section) {
      null -> false
      else -> when(section!!.harmony) {
        null -> true
        else -> false
      }
    }
    removeHarmony.isVisible = when(section) {
      null -> false
      else -> when(section!!.harmony) {
        null -> false
        else -> true
      }
    }
    itemView.backgroundResource = when (section) {
      BeatClockPaletteConsumer.section -> arrayOf(
        R.drawable.orbifold_chord_major,
        R.drawable.orbifold_chord_minor,
        R.drawable.orbifold_chord_dominant,
        R.drawable.orbifold_chord_augmented,
        R.drawable.orbifold_chord_diminished
      )[adapterPosition % 5]
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