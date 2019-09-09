package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PorterDuff
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import com.jonlatane.beatpad.BuildConfig
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.util.smartrecycler.updateSmartHolders
import com.jonlatane.beatpad.view.midi.MidiOutputConfiguration
import com.jonlatane.beatpad.view.palette.filemanagement.PaletteManagementDialog
import io.multifunctions.let
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.net.URI


class BeatScratchToolbar(
  override val storageContext: Context,
  override val viewModel: PaletteViewModel
) : _LinearLayout(storageContext), AnkoLogger, Storage, MidiOutputConfiguration {
  override val configurationContext: Context = storageContext
  val paletteManagement = PaletteManagementDialog(context, viewModel)

  init {
    backgroundColor = context.color(R.color.colorPrimaryDark)
//    arrayOf(playImage, loopImage, stopImage, metronomeImage, keyboardImage, unicornImage, mixerImage).forEach {
//      it.setBounds(0, 0, 80, 80)
//    }
  }

  fun <T: View> T.beatScratchToolbarStyle(): T = this.lparams(matchParent, dip(48)) {
    weight = 1f
  }

  val appButton: ImageButton = imageButton {
    imageResource = when(BuildConfig.FLAVOR) {
      "full" -> R.drawable.beatscratch_icon_toolbar
      else -> R.drawable.beatscratch_icon_free_toolbar
    }
    backgroundResource = R.drawable.toolbar_button_beatscratch
    padding = dip(0)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      appMenu.show()
    }
  }.beatScratchToolbarStyle()

  val paletteTitleMenuItem: MenuItem get() = appMenu.menu.findItem(R.id.paletteName)
  private val appMenu = PopupMenu(context, appButton).apply {
    inflate(R.menu.beatscratch_app_menu)

    applyTypeface()

    setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.newPalette -> paletteManagement.show(PaletteManagementDialog.Mode.NEW)
        R.id.openPalette -> paletteManagement.show(PaletteManagementDialog.Mode.OPEN)
        R.id.duplicatePalette -> paletteManagement.show(PaletteManagementDialog.Mode.DUPLICATE)
        R.id.savePalette -> {
          context.toast("Saving...")
          viewModel.save(showSuccessToast = true)
        }
        R.id.copyPalette -> copyPalette()
        R.id.exportMusicXml, R.id.exportMidi -> context.toast("TODO for subscription 💸!")
        R.id.configureMidiSynthesizers -> midiOutputConfigurationAlert.show()
        else             -> context.toast("TODO!")
      }
      true
    }
    post {
      paletteTitleMenuItem.apply {
        isEnabled = false
        title = Storage.openPaletteFile
      }
    }
    appButton.setOnTouchListener(dragToOpenListener)

  }


  val menuButton: ImageButton = imageButton {
    imageResource = R.drawable.ic_menu_black_24dp
    backgroundResource = R.drawable.toolbar_button_beatscratch
    padding = dip(9)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      val portrait = context.resources.configuration.portrait
      val verticalSectionsVisible = !viewModel.sectionListRecyclerVerticalRotator.isHidden
      val horizontalSectionsVisible = !viewModel.sectionListRecyclerHorizontalRotator.isHidden
//      viewModel.sectionListRecyclerHorizontalRotator.hide(animation = HideAnimation.VERTICAL)
      when {
//        verticalSectionsVisible &&
        !horizontalSectionsVisible -> {
          viewModel.sectionListRecyclerHorizontal.adapter.notifyDataSetChanged()
          viewModel.sectionListRecyclerHorizontalRotator.show(
            animation = if(portrait) HideAnimation.VERTICAL else HideAnimation.HORIZONTAL
          )
          viewModel.sectionListRecyclerHorizontalSpacer?.show(
            animation = if(portrait) HideAnimation.VERTICAL else HideAnimation.HORIZONTAL
          )
          viewModel.sectionListRecyclerVerticalRotator.hide(animation = HideAnimation.HORIZONTAL) {
            post { viewModel.melodyViewModel.onZoomFinished() }
          }
        }
        //horizontalSectionsVisible && !verticalSectionsVisible
        else -> {
          viewModel.sectionListRecyclerVertical.adapter.notifyDataSetChanged()
          viewModel.sectionListRecyclerHorizontalRotator.hide(
            animation = if(portrait) HideAnimation.VERTICAL else HideAnimation.HORIZONTAL
          )
          viewModel.sectionListRecyclerHorizontalSpacer?.hide(
            animation = if(portrait) HideAnimation.VERTICAL else HideAnimation.HORIZONTAL
          )
          viewModel.sectionListRecyclerVerticalRotator.show(animation = HideAnimation.HORIZONTAL) {
            post { viewModel.melodyViewModel.onZoomFinished() }
          }
        }
      }
    }
  }.beatScratchToolbarStyle()

  fun updateButtonColors() {
    viewMode = viewMode
    menuButton.image
      ?.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
  }

  var viewMode = false
  set(value) {
    field = value
    if(value) { viewModeButton to editModeButton } else { editModeButton to viewModeButton }.let {
      activeModeButton, inactiveModeButton ->
      inactiveModeButton?.backgroundResource = R.drawable.toolbar_button_beatscratch
      inactiveModeButton?.image
        ?.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
      activeModeButton?.backgroundResource = R.drawable.toolbar_button_active_instrument
      activeModeButton?.image?.colorFilter = null
    }
    viewModeButton.padding = dip(9)
    editModeButton.padding = dip(9)
  }
  val viewModeButton: ImageButton = imageButton {
    imageResource = R.drawable.view_score
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    isClickable = true
    onClick { context.toast("TODO!") }
  }.beatScratchToolbarStyle()
  val editModeButton: ImageButton = imageButton {
    imageResource = R.drawable.edit_black
    padding = dip(10)
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.beatScratchToolbarStyle()

  init { updateButtonColors() }

  fun copyPalette() {
    val text = viewModel.palette.toURI().toString()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("BeatScratch Palette", text)
    clipboard.setPrimaryClip(clip)
    context.toast("Copied BeatScratch Palette data to clipboard!")
  }

  private fun getClipboardPalette(): Palette? = try {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip?.getItemAt(0)?.text?.let { URI(it.toString()) }
      ?.let { uri ->
        uri.toEntity("palette", "v1", Palette::class)
      }
  } catch(t: Throwable) {
    error("Failed to deserialize palette", t)
    null
  }


//  val shareButton = imageButton {
//    imageResource = R.drawable.ic_share_black_24dp
//    scaleType = ImageView.ScaleType.FIT_CENTER
//    onClick {
//      val text = BeatClockPaletteConsumer.palette?.toURI()?.toString() ?: ""
//      val clipboard = storageContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//      val clip = ClipData.newPlainText("BeatScratch Palette", text)
//      clipboard.primaryClip = clip
//
//      storageContext.toast("Copied BeatScratch Palette data to clipboard!")
//    }
//  }.beatScratchToolbarStyle()
}
