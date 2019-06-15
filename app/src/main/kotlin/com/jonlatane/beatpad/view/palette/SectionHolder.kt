package com.jonlatane.beatpad.view.palette

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.showRenameDialog
import com.jonlatane.beatpad.util.layoutWidth
import org.jetbrains.anko.*
import java.util.*

class SectionHolder(
  val orientation: Int,
  parent: ViewGroup,
  val viewModel: PaletteViewModel
) : RecyclerView.ViewHolder(
  _LinearLayout(parent.context).apply {
    isClickable = true
    isLongClickable = true
    relativeLayout {
      when (orientation) {
        LinearLayoutManager.HORIZONTAL -> {
          val sectionName = textView {
            id = R.id.section_name
            textSize = 25f
            minimumWidth = context.dip(90)
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            typeface = MainApplication.chordTypeface
          }.lparams(wrapContent, wrapContent) {
            alignParentLeft()
            alignParentRight()
            centerVertically()
          }
        }
        else                           -> {
          textView {
            id = R.id.section_name
            textSize = 25f
            singleLine = true
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
            typeface = MainApplication.chordTypeface
          }.lparams(matchParent, wrapContent) {
            alignParentLeft()
            alignParentRight()
            centerVertically()
          }
        }
      }
    }.lparams(
      width = when (orientation) {
        LinearLayoutManager.HORIZONTAL -> wrapContent
        else                           -> matchParent
      },
      height = wrapContent
    ) {
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
    fun sectionDrawableResource(sectionIndex: Int) = arrayOf(
      R.drawable.orbifold_chord_major,
      R.drawable.orbifold_chord_minor,
      R.drawable.orbifold_chord_dominant,
      R.drawable.orbifold_chord_augmented,
      R.drawable.orbifold_chord_diminished
    )[sectionIndex % 5]
    fun sectionColor(sectionIndex: Int) = arrayOf(
      R.color.major,
      R.color.minor,
      R.color.dominant,
      R.color.augmented,
      R.color.diminished
    )[sectionIndex % 5]
    fun sectionColor(section: Section?) = sectionColor(
      section?.let { BeatClockPaletteConsumer.palette?.sections?.indexOf(section) } ?: 0
    )
  }
  val nameTextView: TextView get() = itemView.findViewById(R.id.section_name)
  //val dragHandle: ImageView get() = itemView.findViewById(R.id.section_drag_handle)
  val adapter: SectionListAdapter get() = viewModel.sectionListAdapter!!
  val section: Section?
    get() = when (adapterPosition) {
      viewModel.palette.sections.size -> null
      else                            -> viewModel.palette.sections.getOrNull(adapterPosition)
    }
  val sectionName: TextView by lazy { itemView.findViewById<TextView>(R.id.section_name) }
  val menu: PopupMenu by lazy {
    PopupMenu(parent.context, sectionName).also { popupMenu ->
      popupMenu.inflate(R.menu.section_menu)
      popupMenu.setOnMenuItemClickListener { item ->
        when (item) {
          renameSection -> {
            section?.let { section ->
              itemView.context.showRenameDialog(section.name, "Section") {
                section.name = it
                invalidate()
              }
            }
          }
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
        }
        true
      }
    }
  }
  private val renameSection: MenuItem get() = menu.menu.findItem(R.id.renameSection)
  private val deleteSection: MenuItem get() = menu.menu.findItem(R.id.deleteSection)

  fun invalidate() {
    deleteSection.isVisible = viewModel.palette.sections.size > 1
    itemView.backgroundResource = when (section) {
      BeatClockPaletteConsumer.section -> sectionDrawableResource(adapterPosition)
      else -> R.drawable.orbifold_chord
    }
    itemView.padding = itemView.dip(3)

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
      gravity = if(orientation == LinearLayoutManager.HORIZONTAL) Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
       else Gravity.CENTER_VERTICAL or Gravity.START
    }
    itemView.apply {
      setOnClickListener {
        BeatClockPaletteConsumer.section = section
      }
      setOnLongClickListener {
        menu.show()
        true
      }
    }
  }

  private fun makeAddButton() {
    sectionName.apply {
      text = "+"
      gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
    }
    itemView.apply {
      setOnClickListener {
        adapter.addSection()
      }
      setOnLongClickListener { false }
    }
  }
}