package com.jonlatane.beatpad.view.palette

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.color
import io.multifunctions.let
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class BeatScratchToolbar(
  override val storageContext: Context,
  val viewModel: PaletteViewModel
) : _LinearLayout(storageContext), AnkoLogger, Storage {
  private val metronomeImage = context.resources.getDrawable(R.drawable.noun_metronome_415494_000000, null).apply {
    setBounds(0, 0, 60, 60)
  }

  init {
    backgroundColor = context.color(R.color.colorPrimaryDark)
//    arrayOf(playImage, loopImage, stopImage, metronomeImage, keyboardImage, unicornImage, mixerImage).forEach {
//      it.setBounds(0, 0, 80, 80)
//    }
  }

  fun <T: View> T.beatScratchToolbarStyle(): T = this.lparams(matchParent, dip(48)) {
    weight = 1f
  }

  val appButton = imageButton {
    imageResource = R.drawable.beatscratch_icon_inset
    backgroundResource = R.drawable.toolbar_button_beatscratch
    padding = dip(0)
    scaleType = ImageView.ScaleType.FIT_CENTER
    onClick {
//      imageResource = R.drawable.icons8_skip_to_start_filled_100
//      val startIntent = Intent(MainApplication.instance, PlaybackService::class.java)
//      startIntent.action = if(PlaybackService.instance?.isStopped != false) PlaybackService.Companion.Action.PLAY_ACTION
//      else PlaybackService.Companion.Action.REWIND_ACTION
//      MainApplication.instance.startService(startIntent)
    }
  }.beatScratchToolbarStyle()


  val menuButton: ImageButton = imageButton {
    imageResource = R.drawable.ic_menu_black_24dp
    backgroundResource = R.drawable.toolbar_button_beatscratch
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.beatScratchToolbarStyle()

  fun updateButtonColors() {
    viewMode = viewMode
    menuButton.image
      ?.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
  }

  var viewMode = false
  set(value) {
    field = value
    if(value) { viewButton to editButton } else { editButton to viewButton }.let {
      activeModeButton, inactiveModeButton ->
      inactiveModeButton?.backgroundResource = R.drawable.toolbar_button_beatscratch
      inactiveModeButton?.image
        ?.setColorFilter(BeatClockPaletteConsumer.currentSectionColor, PorterDuff.Mode.SRC_IN)
      activeModeButton?.backgroundResource = R.drawable.toolbar_button_active_instrument
      activeModeButton?.image?.colorFilter = null
    }
  }
  val viewButton: ImageButton = imageButton {
    imageResource = R.drawable.view_score
    padding = dip(7)
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.beatScratchToolbarStyle()
  val editButton: ImageButton = imageButton {
    imageResource = R.drawable.edit_black
    padding = dip(10)
    scaleType = ImageView.ScaleType.FIT_CENTER
  }.beatScratchToolbarStyle()

  init { updateButtonColors() }


//  val shareButton = imageButton {
//    imageResource = R.drawable.ic_share_black_24dp
//    scaleType = ImageView.ScaleType.FIT_CENTER
//    onClick {
//      val text = BeatClockPaletteConsumer.palette?.toURI()?.toString() ?: ""
//      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//      val clip = ClipData.newPlainText("BeatScratch Palette", text)
//      clipboard.primaryClip = clip
//
//      context.toast("Copied BeatScratch Palette data to clipboard!")
//    }
//  }.beatScratchToolbarStyle()
}
