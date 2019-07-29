package com.jonlatane.beatpad.view.palette.filemanagement

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.view.hideableLinearLayout
import com.jonlatane.beatpad.view.hideableRelativeLayout
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class PaletteManagementDialog(val context: Context, val paletteViewModel: PaletteViewModel) {
  private lateinit var titleText: TextView
  private lateinit var paletteRecycler: RecyclerView
  private lateinit var editPaletteName: EditText
  private lateinit var saveButton: Button
  private lateinit var editArea: RelativeLayout
  enum class Mode(
    val titleText: String,
    val buttonText: String?,
    val textEditable: Boolean = true
  ) {
    NEW("New Palette", "Create"),
    DUPLICATE("Save Palette As", "Save"),
    OPEN("Open Palette", "Open", textEditable = false)
  }

  fun show(mode: Mode) {
    titleText.text = mode.titleText
    saveButton.text = mode.buttonText
    saveButton.onClick {
      context.toast("TODO!")
    }
    if(mode == Mode.OPEN) {
      context.toast("Saving Current Palette...")
      paletteViewModel.save(showSuccessToast = true)
    }
    (lengthLayout.parent as? ViewGroup)?.removeView(lengthLayout)
    editPaletteName.isEnabled = mode.textEditable
    alert.show()
  }
  private lateinit var lengthLayout: RelativeLayout
  private val alert = context.alert {
    customView {
      lengthLayout = relativeLayout {
        titleText = textView("Palettes") {
          id = View.generateViewId()
          typeface = MainApplication.chordTypefaceBold
          textSize = 18f
          padding = dip(16)
        }.lparams(wrapContent, wrapContent) {
          centerHorizontally()
          alignParentTop()
        }

        editArea = hideableRelativeLayout {
          padding = dip(16)
          id = View.generateViewId()
          saveButton = button {
            text = "Save"
            typeface = MainApplication.chordTypeface
            backgroundResource = R.drawable.toolbar_button
            id = View.generateViewId()
          }.lparams(wrapContent, wrapContent) {
            alignParentTop()
            alignParentRight()
          }
          editPaletteName = editText {
            id = View.generateViewId()
            typeface = MainApplication.chordTypeface
          }.lparams(matchParent, wrapContent) {
            alignParentTop()
            alignParentLeft()
            leftOf(saveButton)
          }
        }.lparams(matchParent, wrapContent) {
          alignParentLeft()
          alignParentRight()
          alignParentBottom()
        }

        val paletteList = Storage.getPalettes(context)
        paletteRecycler = InstaRecycler.instaRecycler(
          context,
          factory = { nonDelayedRecyclerView().apply { id = View.generateViewId() } },
          itemCount = { paletteList.size },
          binder = { position ->
            findViewById<TextView>(InstaRecycler.example_id).apply {
              text = paletteList[position].name.removeSuffix(".json")
//              val part = availableParts[position]
//              text = part.instrument.instrumentName
//              backgroundResource = when {
//                getSelectedPart() == part                               -> currentSectionDrawable
//                (part.instrument as? MIDIInstrument)?.drumTrack == true -> R.drawable.part_background_drum
//                else                                                    -> R.drawable.part_background
//              }
              padding = dip(16)
              isClickable = true
//              onClick {
//                vibrate(10)
//                setSelectedPart(part)
//                adapter.notifyDataSetChanged()
//              }
            }
          }
        ).also {
          it.id = View.generateViewId()
          it.padding = dip(16)
        }.lparams(matchParent, wrapContent) {
          below(titleText)
          above(editArea)
          alignParentLeft()
          alignParentRight()
          minimumHeight = dip(200)
        }


      }//.lparams(wrapContent, wrapContent)
    }
  }

  @SuppressLint("SetTextI18n")
  fun applyChange() {

  }
}