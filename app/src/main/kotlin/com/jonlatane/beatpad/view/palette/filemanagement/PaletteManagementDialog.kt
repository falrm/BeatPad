package com.jonlatane.beatpad.view.palette.filemanagement

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.PaletteStorage
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.HideableConstraintLayout
import com.jonlatane.beatpad.view.hideableConstraintLayout
import com.jonlatane.beatpad.view.hideableRelativeLayout
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File
import java.net.URI

class PaletteManagementDialog(
  override val storageContext: Context,
  val paletteViewModel: PaletteViewModel
): Storage {
  val paletteList: List<File> get() = Storage.getPalettes(storageContext)
  private lateinit var titleText: TextView
  private lateinit var paletteRecycler: RecyclerView
  private lateinit var editPaletteName: EditText
  private lateinit var saveButton: Button
  private lateinit var editArea: RelativeLayout
  private lateinit var createFromThingsArea: HideableConstraintLayout
  lateinit var createFromIntentCheckbox: CheckBox
  private lateinit var createFromIntentText: TextView
  private lateinit var createFromClipboardCheckbox: CheckBox
  private lateinit var createFromClipboardText: TextView
  enum class Mode(
    val titleText: String,
    val buttonText: String?,
    val defaultText: (Context) -> String = {""},
    val textEditable: Boolean = true,
    val onSubmit: PaletteManagementDialog.(String) -> Unit
  ) {
    NEW("New Palette", "Create",
      defaultText = { context ->
        Section.generateNewName(
          Storage.getPalettes(context).map { it.nameWithoutExtension },
          "palette "
        )
      },
      onSubmit = { name ->
        val doCreate: () -> Unit = { with(Storage) {
          val palette = when {
            createFromIntentCheckbox.isChecked -> MainApplication.intentPalette ?: null.also {
              storageContext.toast("Palette no longer accessible in intents.")
            }
            createFromClipboardCheckbox.isChecked -> getClipboardPalette() ?: null.also {
              storageContext.toast("Palette no longer accessible from clipboard.")
            }
            else -> PaletteStorage.basePalette
          }
          if(palette != null) {
            openPaletteFile = name
            BeatClockPaletteConsumer.palette = palette
            BeatClockPaletteConsumer.viewModel?.palette = BeatClockPaletteConsumer.palette!!
            storageContext.storePalette(showSuccessToast = true)
          }
        } }
        if(Storage.getPalettes(storageContext).map { it.nameWithoutExtension }.contains(name)) {
          showConfirmDialog(
            storageContext,
            promptText = "Palette \"$name\" already exists. Overwrite it?",
            yesText = "Overwrite",
            noText = "Cancel",
            yesAction = doCreate
          )
        } else doCreate()
      }),
    DUPLICATE("Save Palette As", "Save",
      defaultText = { context ->
        Section.generateDuplicateName(
          Storage.getPalettes(context).map { it.nameWithoutExtension },
          Storage.openPaletteFile
        )
      },
      onSubmit = { name ->
        val doSave: () -> Unit = { with(Storage) {
          openPaletteFile = name
          storageContext.storePalette(showSuccessToast = true)
        } }
        if(Storage.getPalettes(storageContext).map { it.nameWithoutExtension }.contains(name)) {
          showConfirmDialog(
            storageContext,
            promptText = "Palette \"$name\" already exists. Overwrite it?",
            yesText = "Overwrite",
            noText = "Cancel",
            yesAction = doSave
          )
        } else doSave()
      }
    ),
    OPEN("Open Palette", "Open", { Storage.openPaletteFile }, textEditable = false,
      onSubmit = { name ->
        Storage.run {
          openPaletteFile = name
          BeatClockPaletteConsumer.palette = storageContext.loadPalette()
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
      val name = editPaletteName.text.toString()
      mode.run { onSubmit(name) }
      paletteRecycler.adapter.notifyDataSetChanged()
      paletteViewModel.beatScratchToolbar.paletteTitleMenuItem.title = name
      //(paletteRecycler.adapter as SmartAdapter).updateSmartHolders()
    }

    if (mode in listOf(Mode.OPEN, Mode.NEW)) {
      storageContext.toast("Saving Current Palette...")
      paletteViewModel.save(showSuccessToast = true)
    }
    when (mode) {
      Mode.NEW -> {
        createFromIntentCheckbox.isEnabled = MainApplication.intentPalette?.let { true }
          ?: false.also {
            createFromIntentCheckbox.isChecked = false
          }
        createFromIntentText.isEnabled = createFromIntentCheckbox.isEnabled

        createFromClipboardCheckbox.isEnabled = getClipboardPalette()?.let { true } ?: false.also {
          createFromClipboardCheckbox.isChecked = false
        }
        createFromClipboardText.isEnabled = createFromClipboardCheckbox.isEnabled
        createFromThingsArea.show()
      }
      else     -> createFromThingsArea.hide()
    }

    (lengthLayout.parent as? ViewGroup)?.removeView(lengthLayout)
    editPaletteName.isEnabled = mode.textEditable
    editPaletteName.text.clear()
    editPaletteName.text.append(mode.defaultText(storageContext))
    paletteRecycler.adapter.notifyDataSetChanged()
    (alert.show() as AlertDialog).apply {
      setOnCancelListener { MainApplication.intentPalette = null }
      setOnDismissListener { MainApplication.intentPalette = null }
    }
  }
  private lateinit var lengthLayout: RelativeLayout
  private val alert = storageContext.alert {
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

        createFromThingsArea = hideableConstraintLayout {
          padding = dip(16)
          id = View.generateViewId()

          createFromIntentCheckbox = checkBox{
            id = View.generateViewId()
            onCheckedChange { _, isChecked ->
              if(isChecked) createFromClipboardCheckbox.isChecked = false
            }
          }.lparams(wrapContent, wrapContent)

          createFromIntentText = textView{
            id = View.generateViewId()
            text = "Create from the opened URL"
            typeface = MainApplication.chordTypeface
            singleLine = true
            gravity = Gravity.START
            isClickable = true
            onClick {
              createFromIntentCheckbox.isChecked = !createFromIntentCheckbox.isChecked
            }
          }.lparams(0, wrapContent)

          createFromClipboardCheckbox = checkBox {
            id = View.generateViewId()
            onCheckedChange { _, isChecked ->
              if(isChecked) createFromIntentCheckbox.isChecked = false
            }
          }.lparams(wrapContent, wrapContent)

          createFromClipboardText = textView{
            id = View.generateViewId()
            text = "Create from the clipboard"
            typeface = MainApplication.chordTypeface
            singleLine = true
            gravity = Gravity.START
            isClickable = true
            onClick {
              createFromClipboardCheckbox.isChecked = !createFromClipboardCheckbox.isChecked
            }
          }.lparams(0, wrapContent)

          applyConstraintSet {
            createFromIntentCheckbox {
              connect(
                TOP to TOP of ConstraintSet.PARENT_ID,
                START to START of ConstraintSet.PARENT_ID margin dip(15)
              )
            }
            createFromIntentText {
              connect(
                TOP to TOP of createFromIntentCheckbox,
                START to END of createFromIntentCheckbox margin dip(15),
                END to END of ConstraintSet.PARENT_ID margin dip(15),
                BOTTOM to BOTTOM of createFromIntentCheckbox
              )
            }
            createFromClipboardCheckbox {
              connect(
                TOP to BOTTOM of createFromIntentCheckbox margin dip(15),
                START to START of ConstraintSet.PARENT_ID margin dip(15)
              )
            }
            createFromClipboardText {
              connect(
                TOP to TOP of createFromClipboardCheckbox,
                START to END of createFromClipboardCheckbox margin dip(15),
                END to END of ConstraintSet.PARENT_ID margin dip(15),
                BOTTOM to BOTTOM of createFromClipboardCheckbox
              )
            }
          }
        }.lparams(matchParent, wrapContent) {
          alignParentLeft()
          alignParentRight()
          alignParentBottom()
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
          above(createFromThingsArea)
        }

        paletteRecycler = InstaRecycler.instaRecycler(
          context,
          factory = { nonDelayedRecyclerView().apply { id = View.generateViewId() } },
          itemCount = { paletteList.size },
          binder = { position ->
            val name = paletteList[position].nameWithoutExtension
            findViewById<TextView>(InstaRecycler.example_id).apply {
              text = name
              textColor = if(name == Storage.openPaletteFile)
                BeatClockPaletteConsumer.currentSectionColor
              else R.color.black
              padding = dip(16)
              typeface = MainApplication.chordTypefaceRegular
              isClickable = true
              onClick {
                vibrate(10)
                editPaletteName.text.apply {
                  clear()
//                  val fillStuff = if(mode == Mode.OPEN) name else Section.generateDuplicateName(
//                    Storage.getPalettes(context).map { it.nameWithoutExtension },
//                    name
//                  )
                  append(name)
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

  fun getClipboardPalette(): Palette? = try {
    val clipboard = MainApplication.instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip?.getItemAt(0)?.text?.let { URI(it.toString()) }
      ?.let { uri ->
        uri.toEntity("palette", "v1", Palette::class)
      }
  } catch(t: Throwable) {
    error("Failed to deserialize palette", t)
    null
  }
}