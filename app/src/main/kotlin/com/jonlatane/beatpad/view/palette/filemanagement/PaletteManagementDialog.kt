package com.jonlatane.beatpad.view.palette.filemanagement

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.hideableRelativeLayout
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

class PaletteManagementDialog(val context: Context, val paletteViewModel: PaletteViewModel) {
  val paletteList: List<File> get() = Storage.getPalettes(context)
  private lateinit var titleText: TextView
  private lateinit var paletteRecycler: RecyclerView
  private lateinit var editPaletteName: EditText
  private lateinit var saveButton: Button
  private lateinit var editArea: RelativeLayout
  enum class Mode(
    val titleText: String,
    val buttonText: String?,
    val defaultText: (Context) -> String = {""},
    val textEditable: Boolean = true,
    val onSubmit: (String, Context) -> Unit = { _, _ -> }
  ) {
    NEW("New Palette", "Create",
      defaultText = { context ->
        Section.generateNewName(
          Storage.getPalettes(context).map { it.nameWithoutExtension },
          Storage.openPaletteFile
        )
      },
      onSubmit = { name, context ->
        val doSave: () -> Unit = { with(Storage) {
          openPaletteFile = name
          BeatClockPaletteConsumer.palette = PaletteStorage.basePalette
          BeatClockPaletteConsumer.viewModel?.palette = BeatClockPaletteConsumer.palette!!
          context.storePalette(showSuccessToast = true)
        } }
        if(Storage.getPalettes(context).map { it.nameWithoutExtension }.contains(name)) {
          showConfirmDialog(
            context,
            promptText = "Palette \"$name\" already exists. Overwrite it?",
            yesText = "Overwrite",
            noText = "Cancel",
            yesAction = doSave
          )
        } else doSave()
      }),
    DUPLICATE("Save Palette As", "Save",
      defaultText = { context ->
        Section.generateDuplicateName(
          Storage.getPalettes(context).map { it.nameWithoutExtension },
          Storage.openPaletteFile
        )
      },
      onSubmit = { name, context ->
        val doSave: () -> Unit = { with(Storage) {
          openPaletteFile = name
          context.storePalette(showSuccessToast = true)
        } }
        if(Storage.getPalettes(context).map { it.nameWithoutExtension }.contains(name)) {
          showConfirmDialog(
            context,
            promptText = "Palette \"$name\" already exists. Overwrite it?",
            yesText = "Overwrite",
            noText = "Cancel",
            yesAction = doSave
          )
        } else doSave()
      }
    ),
    OPEN("Open Palette", "Open", { Storage.openPaletteFile }, textEditable = false,
      onSubmit = { name, context ->
        Storage.run {
          openPaletteFile = name
          BeatClockPaletteConsumer.palette = context.loadPalette()
          BeatClockPaletteConsumer.viewModel?.palette = BeatClockPaletteConsumer.palette!!
        }
      })
  }

  private var mode: Mode = Mode.NEW
  fun show(mode: Mode) {
    this.mode = mode
    titleText.text = mode.titleText
    saveButton.text = mode.buttonText
    saveButton.onClick {
      mode.onSubmit(editPaletteName.text.toString(), context)
    }
    if(mode == Mode.OPEN) {
      context.toast("Saving Current Palette...")
      paletteViewModel.save(showSuccessToast = true)
    }
    (lengthLayout.parent as? ViewGroup)?.removeView(lengthLayout)
    editPaletteName.isEnabled = mode.textEditable
    editPaletteName.text.clear()
    editPaletteName.text.append(mode.defaultText(context))
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

        paletteRecycler = InstaRecycler.instaRecycler(
          context,
          factory = { nonDelayedRecyclerView().apply { id = View.generateViewId() } },
          itemCount = { paletteList.size },
          binder = { position ->
            val name = paletteList[position].name.removeSuffix(".json")
            findViewById<TextView>(InstaRecycler.example_id).apply {
              text = name
              padding = dip(16)
              isClickable = true
              onClick {
                vibrate(10)
                editPaletteName.text.apply {
                  clear()
                  val fillStuff = if(mode == Mode.OPEN) name else Section.generateDuplicateName(
                    Storage.getPalettes(context).map { it.nameWithoutExtension },
                    name
                  )
                  append(fillStuff)
                }
              }
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
}