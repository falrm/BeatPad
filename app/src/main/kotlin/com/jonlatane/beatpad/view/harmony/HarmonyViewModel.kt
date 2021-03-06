package com.jonlatane.beatpad.view.harmony

import BeatClockPaletteConsumer
import android.content.ClipboardManager
import android.content.Context
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.showConfirmDialog
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.ZoomableRecyclerView
import com.jonlatane.beatpad.view.palette.PaletteViewModel
import org.jetbrains.anko.error
import org.jetbrains.anko.toast
import java.net.URI

open class HarmonyViewModel(
  override val storageContext: Context
): SelectedChordAnimation, Storage {
  var paletteViewModel: PaletteViewModel? = null
  var harmonyView: HarmonyView? = null
  lateinit var beatAdapter: HarmonyBeatAdapter
  //var selectedChord: Chord? = null
  var harmonyElementRecycler: ZoomableRecyclerView? = null
//  set(value) {
//    field = value
//    notifyHarmonyChanged()
//  }
  val section: Section? get() = BeatClockPaletteConsumer.section
  val harmony: Harmony? get() = BeatClockPaletteConsumer.harmony
  override var isChoosingHarmonyChord: Boolean = false
  set(value) {
    field = value
    if (value) {
      val wasOrbifoldVisible = paletteViewModel?.orbifold?.isHidden == false
      paletteViewModel?.orbifold?.canEditChords = true
      paletteViewModel?.showOrbifold()
      paletteViewModel?.backStack?.push {
        if (isChoosingHarmonyChord) {
          isChoosingHarmonyChord = false
          if(!wasOrbifoldVisible) paletteViewModel?.hideOrbifold()
          selectedHarmonyElements = null
          true
        } else false
      }
      animateBeatsOfSelectedChord()
    } else {
      paletteViewModel?.orbifold?.canEditChords = false
      paletteViewModel?.melodyViewModel?.updateMelodyDisplay()
    }
  }

  var selectedHarmonyElements: IntRange? = null
  internal set(value) {
    field = value
    notifyHarmonyChanged()
  }
  /**
   * Get: get the current chord being edited (per [isChoosingHarmonyChord]/[selectedHarmonyElements]),
   *      or null if a chord isn't being edited
   * Set:
   *      When [selectedHarmonyElements] is not null, sets the chord at the beginning of the selection.
   *      When [selectedHarmonyElements] is null, setting to anything other than null is an error.
   */
  var editingChord: Chord?
  get() = selectedHarmonyElements?.let {
    harmony!!.changeBefore(it.first)
  }
  set(value) {
    selectedHarmonyElements?.let {
      harmony!!.changes[it.first] = value
    }
    notifyHarmonyChanged()
    paletteViewModel?.melodyBeatAdapter?.notifyDataSetChanged()
  }
  fun notifyHarmonyChanged() {
    beatAdapter.updateSmartHolders()
    paletteViewModel?.melodyViewModel?.beatAdapter?.updateSmartHolders()
    harmonyView?.syncScrollingChordText()
    paletteViewModel?.melodyViewModel?.melodyView?.syncScrollingChordText()
  }
  fun pasteHarmony(section: Section? = this@HarmonyViewModel.section) {
    getClipboardHarmony()?.let { newHarmony ->
      importHarmony(newHarmony, section)
    } ?: storageContext.toast("Failed to read Harmony from clipboard.")
  }

  fun importHarmony(newHarmony: Harmony, section: Section? = this@HarmonyViewModel.section) {
    val doImport = {
      section?.harmony = newHarmony
      notifyHarmonyChanged()
    }
    if(harmony?.changes?.keys?.size ?: 1 > 1) {
      showConfirmDialog(
        storageContext,
        "This will delete the existing Harmony.",
        "Yes, overwrite"
      ) { doImport() }
    } else {
      doImport()
    }
  }

  fun getClipboardHarmony(): Harmony? = try {
    val clipboard = MainApplication.instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.primaryClip?.getItemAt(0)?.text?.let { URI(it.toString()) }
      ?.let { uri ->
        uri.toEntity("harmony", "v1", Harmony::class)
      }
  } catch(t: Throwable) {
    error("Failed to deserialize harmony", t)
    null
  }
}