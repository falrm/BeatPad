package com.jonlatane.beatpad.view.palette.filemanagement

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*

class PaletteManagementDialog(context: Context, val paletteViewModel: PaletteViewModel) {
  private lateinit var titleText: TextView
  private lateinit var paletteRecycler: RecyclerView
  private lateinit var editPaletteName: EditText
  enum class Mode(
    val title: String
  ) {
    NEW("New Palette"),
    DUPLICATE("Save Palette As"),
    OPEN("Open Palette")
  }

  fun show(mode: Mode) {
    titleText.text = mode.title
    if(mode == Mode.OPEN) {
      paletteViewModel.save()
    }
    (lengthLayout.parent as? ViewGroup)?.removeView(lengthLayout)
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

        paletteRecycler = InstaRecycler.instaRecycler(
          context,
          factory = { nonDelayedRecyclerView().apply { id = View.generateViewId() } },
          itemCount = { /*availableParts.count()*/ 1 },
          binder = { position ->
            findViewById<TextView>(InstaRecycler.example_id).apply {
              text = "Hi"
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
          alignParentLeft()
          alignParentRight()
          minimumHeight = dip(200)
        }

        linearLayout {
          orientation = LinearLayout.HORIZONTAL
          padding = dip(16)

          editPaletteName = editText {
            id = View.generateViewId()
            typeface = MainApplication.chordTypeface
          }.lparams(matchParent, wrapContent) {
            weight = 1f
          }

          val saveButton = button {
            text = "Save"
            typeface = MainApplication.chordTypeface
            backgroundResource = R.drawable.toolbar_button
          }.lparams(wrapContent, wrapContent) {
            weight = 0f
          }
        }.lparams(matchParent, wrapContent) {
          below(paletteRecycler)
          alignParentLeft()
        }




      }//.lparams(wrapContent, wrapContent)
    }
  }

  @SuppressLint("SetTextI18n")
  fun applyChange() {

  }
}