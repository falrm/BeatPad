package com.jonlatane.beatpad.view.palette

import BeatClockPaletteConsumer
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.jonlatane.beatpad.BuildConfig
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.view.midi.MidiOutputConfiguration
import com.jonlatane.beatpad.view.palette.filemanagement.PaletteManagementDialog
import io.multifunctions.let
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.lang.Thread.sleep
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
      vibrate(10, 100)
      updateAppMenu()
      appMenu.show()
    }
  }.beatScratchToolbarStyle()

  fun updateAppMenu() {
    paletteTitleMenuItem.title = Storage.openPaletteFile
    signInMenuItem.title = FirebaseAuth.getInstance().currentUser?.let {
      "Sign out ${it.displayName ?: "user"}..."
    } ?: "Sign in..."
    appMenu.applyTypeface()
  }

  private val paletteTitleMenuItem: MenuItem get() = appMenu.menu.findItem(R.id.paletteName)
  private val signInMenuItem: MenuItem get() = appMenu.menu.findItem(R.id.signIn)
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
        R.id.signIn -> FirebaseAuth.getInstance().currentUser?.let {
          showConfirmDialog(context, "Really sign out?") { viewModel.activity.signOut() }
        } ?: viewModel.activity.signIn()
        R.id.quitApplication -> showConfirmDialog(
          context,
          "Really quit BeatScratch?",
          yesText = "Quit"
        ) {
          val intent = Intent(context, PlaybackService::class.java)
          intent.action = PlaybackService.Companion.Action.STOPFOREGROUND_ACTION
          context.startService(intent)
        }
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


  val playPauseSectionDisplayButton: ImageButton = imageButton {
    imageResource = R.drawable.ic_menu_black_24dp
    backgroundResource = R.drawable.toolbar_button_beatscratch
    padding = dip(9)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      vibrate(10, 100)
      when(interactionMode) {
        InteractionMode.EDIT -> {
          swapSectionListDisplayModes()
        }
        InteractionMode.VIEW -> {
          togglePlayStop()
        }
      }
    }
  }.beatScratchToolbarStyle()

  private val shouldPlay: Boolean get() = PlaybackService.instance?.isStopped != false

  private fun togglePlayStop() {
    playPauseSectionDisplayButton.imageResource = when(interactionMode) {
      InteractionMode.EDIT -> R.drawable.ic_menu_black_24dp
      InteractionMode.VIEW -> if(shouldPlay) R.drawable.icons8_stop_32
      else R.drawable.icons8_play_32
    }
    updateButtonColors()
    val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
    startIntent.action = if(shouldPlay) PlaybackService.Companion.Action.PLAY_ACTION
    else PlaybackService.Companion.Action.STOP_ACTION
    MainApplication.instance.startService(startIntent)
  }

  private fun swapSectionListDisplayModes() {
    val portrait = context.resources.configuration.portrait
    val verticalSectionsVisible = !viewModel.sectionListRecyclerVerticalRotator.isHidden
    val horizontalSectionsVisible = !viewModel.sectionListRecyclerHorizontalRotator.isHidden
//      viewModel.sectionListRecyclerHorizontalRotator.hide(animation = HideAnimation.VERTICAL)
    val doZoomFinished = incrementUntil(2) {
      doAsync {
        sleep(300L)
        uiThread {
          viewModel.melodyViewModel.onZoomFinished()
        }
      }
    }
    when {
//        verticalSectionsVisible &&
      !horizontalSectionsVisible -> {
        viewModel.sectionListRecyclerHorizontal.adapter!!.notifyDataSetChanged()
        viewModel.showHorizontalSectionList { doZoomFinished() }
        viewModel.hideVerticalSectionList { doZoomFinished() }
      }
      //horizontalSectionsVisible && !verticalSectionsVisible
      else -> {
        viewModel.sectionListRecyclerVertical.adapter!!.notifyDataSetChanged()
        viewModel.hideHorizontalSectionList{ doZoomFinished() }
        viewModel.showVerticalSectionList { doZoomFinished() }
      }
    }
  }

  fun updateButtonColors() {
//    interactionMode = interactionMode
    when(interactionMode) {
      InteractionMode.VIEW -> viewModeButton to editModeButton
      InteractionMode.EDIT -> editModeButton to viewModeButton
    }.let {
      activeModeButton, inactiveModeButton ->
      inactiveModeButton?.backgroundResource = R.drawable.toolbar_button_beatscratch
      inactiveModeButton?.image
        ?.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
      activeModeButton?.backgroundResource = R.drawable.toolbar_button_active
      activeModeButton?.image?.colorFilter = null
    }
    viewModeButton.padding = dip(9)
    editModeButton.padding = dip(9)
    playPauseSectionDisplayButton.image
      ?.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
  }

  enum class InteractionMode {
    VIEW {
      override val playbackMode = BeatClockPaletteConsumer.PlaybackMode.PALETTE
    },
    EDIT {
      override var playbackMode = BeatClockPaletteConsumer.PlaybackMode.SECTION
    };
    abstract val playbackMode: BeatClockPaletteConsumer.PlaybackMode
  }
  var interactionMode: InteractionMode = InteractionMode.EDIT
  set(value) {
    val changed = field != value
    field = value
    BeatClockPaletteConsumer.playbackMode = value.playbackMode
    if(changed) {
      playPauseSectionDisplayButton.imageResource = when(value) {
        InteractionMode.EDIT -> R.drawable.ic_menu_black_24dp
        InteractionMode.VIEW -> if(shouldPlay) R.drawable.icons8_play_32
        else R.drawable.icons8_stop_32
      }
      updateButtonColors()
      viewModel.notifyInteractionModeChanged()
    } else {
      if(value == InteractionMode.VIEW) {
        viewModel.toggleStaffConfigurationToolbarVisible()
      } else {
        viewModel.toggleSectionOpenInMelodyView()
      }
    }
  }
  val viewModeButton: ImageButton = imageButton {
    imageResource = R.drawable.view_score
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
    isClickable = true
    onClick {
      vibrate(10, 100)
      interactionMode = InteractionMode.VIEW
    }
  }.beatScratchToolbarStyle()
  val editModeButton: ImageButton = imageButton {
    imageResource = R.drawable.edit_black
    padding = dip(10)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
      vibrate(10, 100)
      interactionMode = InteractionMode.EDIT
    }
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
