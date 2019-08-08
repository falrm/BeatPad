package com.jonlatane.beatpad.view.palette

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.showRenameDialog
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.applyTypeface
import com.jonlatane.beatpad.util.smartrecycler.SmartAdapter
import com.jonlatane.beatpad.util.smartrecycler.updateSmartHolders
import org.jetbrains.anko.*
import java.util.*

class SectionHolder(
  val orientation: Int,
  parent: ViewGroup,
  val viewModel: PaletteViewModel,
  val adapter: SectionListAdapter
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
            textScaleX = 0.9f
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
            textScaleX = 0.9f
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
      height = when (orientation) {
        LinearLayoutManager.HORIZONTAL -> matchParent
        else                           -> wrapContent
      }
    ) {
      setMargins(
        dip(10),
        dip(2),
        dip(10),
        dip(2)
      )
    }
  }
), SmartAdapter.Holder, Storage {
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

  override val storageContext: Context = parent.context
  val nameTextView: TextView get() = itemView.findViewById(R.id.section_name)
  //val dragHandle: ImageView get() = itemView.findViewById(R.id.section_drag_handle)
  val section: Section?
    get() = when (adapterPosition) {
      viewModel.palette.sections.size -> null
      else                            -> viewModel.palette.sections.getOrNull(adapterPosition)
    }
  val sectionName: TextView by lazy { itemView.findViewById<TextView>(R.id.section_name) }
  val menu: PopupMenu by lazy {
    PopupMenu(parent.context, sectionName).also { popupMenu ->
      popupMenu.inflate(R.menu.section_menu)
      popupMenu.applyTypeface()
      popupMenu.setOnMenuItemClickListener { item ->
        when (item) {
          renameSection -> {
            section?.let { section ->
              itemView.context.showRenameDialog(section.name, "Section") {
                section.name = it
                this.updateSmartHolder()
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
                viewModel.sectionListAdapters.forEach {
                  it.notifyItemRemoved(adapterPosition)
                  it.notifyItemRangeChanged(
                    adapterPosition,
                    viewModel.palette.sections.size - adapterPosition
                  )
                }
                if (originalSection == BeatClockPaletteConsumer.section) {
                  BeatClockPaletteConsumer.section = viewModel.palette.sections.getOrElse(originalPosition - 1) {
                    _ -> viewModel.palette.sections[0]
                  }
                }
              }
            }
          }
          duplicateSection -> section?.let { section ->
            val copiedSection = section.copy(
              id = UUID.randomUUID(),
              name = Section.generateDuplicateSectionName(
                viewModel.palette.sections,
                basis = section.name
              ),
              harmony = section.harmony!!.copy(changes = TreeMap(section.harmony!!.changes)),
              relatedSections = (section.relatedSections + section.id).toMutableSet(),
              melodies = section.melodies.map {
                it.copy(melody = it.melody)
              }.toMutableSet()
            )
            adapter.addSection(section = copiedSection, position = adapterPosition + 1)
          }
          copyHarmony -> {
              val text = section?.harmony?.toURI()?.toString() ?: ""
              val clipboard = parent.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("BeatScratch Harmony", text)
              clipboard.setPrimaryClip(clip)
              //clipboard.primaryClip = clip
              parent.context.toast("Copied BeatScratch Harmony data to clipboard!")
          }
          pasteHarmony -> viewModel.harmonyViewModel.pasteHarmony(section)
          else -> parent.context.toast("TODO")
        }
        true
      }
    }
  }
  private val renameSection: MenuItem get() = menu.menu.findItem(R.id.renameSection)
  private val deleteSection: MenuItem get() = menu.menu.findItem(R.id.deleteSection)
  private val duplicateSection: MenuItem get() = menu.menu.findItem(R.id.duplicateSection)
  private val copyHarmony: MenuItem get() = menu.menu.findItem(R.id.copySectionHarmony)
  private val pasteHarmony: MenuItem get() = menu.menu.findItem(R.id.pasteSectionHarmony)

  override fun updateSmartHolder() {
    deleteSection.isEnabled = viewModel.palette.sections.size > 1
    itemView.backgroundResource = when (section) {
      BeatClockPaletteConsumer.section -> sectionDrawableResource(adapterPosition)
      else -> R.drawable.orbifold_chord
    }
    itemView.padding = itemView.dip(3)
    pasteHarmony.isEnabled = viewModel.harmonyViewModel.getClipboardHarmony() != null

    when {
      adapterPosition < 0 -> {}
      adapterPosition < viewModel.palette.sections.size -> makeEditableSection()
      adapterPosition >= viewModel.palette.sections.size -> makeAddButton()
    }
    sectionName.requestLayout()
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
        viewModel.sectionListAdapters.forEach { it.recyclerView.updateSmartHolders() }
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