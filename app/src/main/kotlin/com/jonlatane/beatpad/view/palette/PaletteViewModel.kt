package com.jonlatane.beatpad.view.palette

//import com.jonlatane.beatpad.util.syncPositionTo
import BeatClockPaletteConsumer
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.jonlatane.beatpad.PaletteEditorActivity
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.output.service.PlaybackService
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.util.smartrecycler.viewHolders
import com.jonlatane.beatpad.view.HideableLinearLayout
import com.jonlatane.beatpad.view.HideableRecyclerView
import com.jonlatane.beatpad.view.RotateLayout
import com.jonlatane.beatpad.view.colorboard.ColorboardInputView
import com.jonlatane.beatpad.view.harmony.HarmonyView
import com.jonlatane.beatpad.view.harmony.HarmonyViewModel
import com.jonlatane.beatpad.view.keyboard.KeyboardView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import com.jonlatane.beatpad.view.orbifold.RhythmAnimations
import org.jetbrains.anko.*
import java.util.*
import kotlin.properties.Delegates.observable

/**
 * The PaletteViewModel still assumes we'll only be editing
 * one Melody at a time.
 */
class PaletteViewModel constructor(
  override val storageContext: Context,
  val activity: PaletteEditorActivity
) : AnkoLogger, Storage {
  init {
    //BeatClockPaletteConsumer.viewModel = this
  }

  val interactionMode get() = beatScratchToolbar.interactionMode
  fun notifyInteractionModeChanged() {
    when(interactionMode) {
      BeatScratchToolbar.InteractionMode.EDIT -> {
        melodyViewVisible = false
        listOf<HideableView>(
          paletteToolbar,
          sectionListRecyclerHorizontalRotator
        ).forEach { it.show() }
      }
      BeatScratchToolbar.InteractionMode.VIEW -> {
        melodyViewVisible = true
        listOf<HideableView>(
          paletteToolbar,
          sectionListRecyclerHorizontalRotator
        ).forEach { it.hide() }
        sectionListRecyclerVerticalRotator.hide(animation = HideAnimation.HORIZONTAL)
      }
    }
  }
  fun save(showSuccessToast: Boolean = false) = storageContext.storePalette(palette, showSuccessToast = showSuccessToast)
  private var lastSaveTime = System.currentTimeMillis()
  private var lastSaveRequestTime = System.currentTimeMillis()
  fun saveAfterDelay(delay: Long = 7000) {
    lastSaveRequestTime = System.currentTimeMillis()
    doAsync {
      Thread.sleep(delay)
      synchronized(::lastSaveTime) {
        if(lastSaveTime < lastSaveRequestTime && System.currentTimeMillis() - lastSaveRequestTime >= delay) {
          lastSaveTime = System.currentTimeMillis()
          save()
        }
      }
    }
  }

  var playbackTick by observable<Int?>(null) { _, old, new ->
    arrayOf(old, new).filterNotNull().map { tickPosition ->
      (tickPosition.toDouble() / BeatClockPaletteConsumer.ticksPerBeat).toInt()
    }.toSet().forEach { melodyBeat ->
      melodyViewModel.beatAdapter.invalidate(melodyBeat)
      harmonyViewModel.beatAdapter.invalidate(melodyBeat)
    }
  }

  val melodyViewModel = MelodyViewModel(this)
  val melodyView
    get() = melodyViewModel.melodyView
//    set(value) {
//      melodyViewModel.melodyView = value
//    }
  var melodyBeatAdapter
    get() = melodyViewModel.beatAdapter
    set(value) {
      melodyViewModel.beatAdapter = value
    }

  val harmonyViewModel = HarmonyViewModel(storageContext)
    .apply { paletteViewModel = this@PaletteViewModel }
  var harmonyView: HarmonyView
    get() = harmonyViewModel.harmonyView!!
    set(value) { harmonyViewModel.harmonyView = value }

  lateinit var orbifold: OrbifoldView

  var palette: Palette by observable(initialValue = Palette()) { _, _, new ->
    editingMelody = null
    if (new.parts.isEmpty()) {
      new.parts.add(Part())
    }
    if (new.parts.isEmpty()) {
      new.sections.add(Section())
    }
    keyboardPart = new.keyboardPart ?: new.parts[0]
    colorboardPart = new.colorboardPart ?: new.parts[0]
    splatPart = new.splatPart ?: new.parts[0]
    orbifold.orbifold = new.orbifold
    orbifold.chord = new.chord
    paletteToolbar.updateTempoDisplay()
    partListAdapters.forEach { it.notifyDataSetChanged() }
    sectionListAdapters.forEach { it.notifyDataSetChanged() }
    if(!new.sections.contains(BeatClockPaletteConsumer.section)) {
      BeatClockPaletteConsumer.section = new.sections.first()
    }
  }

  var editingMix: Boolean by observable(false) { _, _, editingVolume ->
    partListAdapters.forEach { partListAdapter ->
      partListAdapter.boundViewHolders.forEach { it.editingVolume = editingVolume }
    }
    if (editingVolume) {
      backStack.push {
        if (editingMix) {
          editingMix = false
          true
        } else false
      }
    }
  }

  var editingMelody: Melody<*>? by observable<Melody<*>?>(null) { _, old, new ->
    melodyViewModel.openedMelody = new
    if (new != null && !melodyViewVisible) {
      colorboardView.hide()
      keyboardView.hide()
      harmonyViewModel.beatAdapter
        .syncPositionTo(melodyViewModel.melodyRecyclerView)

      backStack.push {
        if(melodyViewVisible) {
          melodyViewVisible = false
          editingMelody = null
          true
        } else false
      }
      melodyViewVisible = true
    } else {
      if(old != new) {
        saveAfterDelay()
      }
    }
  }

  lateinit var sectionListRecyclerHorizontalRotator: RotateLayout
  lateinit var sectionListRecyclerHorizontal: RecyclerView
  var sectionListRecyclerHorizontalSpacer: HideableLinearLayout? = null
  lateinit var sectionListRecyclerVerticalRotator: RotateLayout
  lateinit var sectionListRecyclerVertical: HideableRecyclerView
  var partListAdapters: MutableList<PartListAdapter> = mutableListOf()
  var sectionListAdapters: MutableList<SectionListAdapter> = mutableListOf()
  lateinit var partListView: PartListView
  lateinit var partListTransitionView: TextView
  lateinit var beatScratchToolbar: BeatScratchToolbar
  lateinit var paletteToolbar: PaletteToolbar
  lateinit var keyboardView: KeyboardView
  lateinit var colorboardView: ColorboardInputView
  var keyboardPart by observable<Part?>(null) { _, _, new ->
    if (new != null) keyboardView.ioHandler.instrument = new.instrument
    palette.keyboardPart = new
    val keyboardDrumTrack = (new?.instrument as? MIDIInstrument)?.drumTrack == true
    if(keyboardDrumTrack) {
      keyboardView.ioHandler.highlightChord(null)
    } else {
      keyboardView.ioHandler.highlightChord(orbifold.chord)
    }
  }
  var colorboardPart: Part? by observable<Part?>(null) { _, _, new ->
    if (new != null) colorboardView.instrument = new.instrument
    palette.colorboardPart = new
  }

  var splatController: DeviceOrientationInstrument? = null
  var splatPart: Part? by observable<Part?>(null) { _, _, new ->
    if (new != null) {
      splatController = DeviceOrientationInstrument(new.instrument).also {
        it.tones = orbifold.chord.getTones()
        RhythmAnimations.wireMelodicControl(orbifold, it)
      }
    }
    palette.splatPart = new
  }

  val backStack: Deque<() -> Boolean> = LinkedList<() -> Boolean>()
  fun onBackPressed(): Boolean = when {
    backStack.isNotEmpty() -> {
      var result = false
      while(backStack.isNotEmpty() && !result) {
        result = backStack.removeFirst()()
      }
      result
    }
    else                                    -> false
  }

  fun notifySectionChange() {
    harmonyViewModel.apply {
      notifyHarmonyChanged()
      isChoosingHarmonyChord = false
      selectedHarmonyElements = null
    }
    beatScratchToolbar.updateButtonColors()

    melodyViewModel.updateToolbarsAndMelody()
//    melodyViewModel.beatAdapter.updateSmartHolders()
//    melodyViewModel.melodyReferenceToolbar.updateButtonText()
    if(editingMix) { // Trigger an update of the mix state.
      editingMix = editingMix
    }
    updateMelodyReferences()
    PlaybackService.instance?.showNotification()
  }

  var melodyViewVisible: Boolean = false
    set(visible) {
      if(field != visible) {
        field = visible
        if(visible) {
          showMelodyView()
        } else {
          hideMelodyView()
        }
      }
    }

  private fun showMelodyView() {
    harmonyView.hide()
    partListView.viewHolders<PartHolder>().mapNotNull { partHolder ->
      partHolder.layout.melodyReferenceRecycler.viewHolders<MelodyReferenceHolder>()
        .firstOrNull { it.melody != null && it.melody == editingMelody }
    }.firstOrNull()?.let { melodyReferenceHolder ->
      val name = melodyReferenceHolder.layout.name
      val partListLocation = intArrayOf(-1, -1)
      val nameLocation = intArrayOf(-1, -1)
      name.getLocationOnScreen(nameLocation)
      if(melodyView.translationX != 0f) {
        partListView.getLocationOnScreen(partListLocation)
        //partListView.animate().alpha(0f).start()
        partListTransitionView.apply {
          alpha = 1f
          translationX = nameLocation[0].toFloat() - partListLocation[0]
          translationY = nameLocation[1].toFloat() - partListLocation[1]
          layoutWidth = name.width
          layoutHeight = name.height
          //animate().alpha(1f).withEndAction {
          post {
            partListTransitionView.animateWidth(partListView.width)
            partListTransitionView.animateHeight(partListView.height + orbifold.height)
            partListTransitionView.animate().translationX(0f).translationY(0f)
              .withEndAction {
                melodyViewModel.melodyView.let { melodyView ->
                  melodyView.alpha = 0f
                  melodyView.translationX = 0f
                  melodyView.animate().alpha(1f).withEndAction {
                    partListTransitionView.alpha = 0f
                  }.start()
                }
              }.start()
          }
          //}.start()
        }
      }
    } ?: showMelodyViewBoring()
  }

  private fun showMelodyViewBoring() {
//    partListTransitionView.apply {
//      translationX = partListView.width.toFloat()
//      translationY = 0f
//      layoutWidth = partListView.width
//      layoutHeight = partListView.height
//      animate().translationX(0f).start()
//    }
    if(melodyViewModel.melodyView.translationX > melodyViewModel.melodyView.width.toFloat()) {
      melodyViewModel.melodyView.translationX = melodyViewModel.melodyView.width.toFloat()
    }
    melodyViewModel.melodyView.animate()
      .translationX(0f)
      .alpha(1f)
      .withEndAction {
        //partListTransitionView.animate().alpha(0f).start()
      }
      .start()
    //partListView.animate().alpha(0f).start()
  }

  private fun hideMelodyView(oldValue: Melody<*>? = editingMelody) {
    harmonyView.show()
    partListView.viewHolders<PartHolder>().mapNotNull { partHolder ->
      partHolder.layout.melodyReferenceRecycler.viewHolders<MelodyReferenceHolder>()
        .firstOrNull { !it.isAddButton && it.melody == oldValue }
    }.firstOrNull()?.let { melodyReferenceHolder ->
      val name = melodyReferenceHolder.layout.name
      val partListLocation = intArrayOf(-1, -1)
      val nameLocation = intArrayOf(-1, -1)
      name.getLocationOnScreen(nameLocation)
      //partListView.animate().alpha(1f)
      partListView.getLocationOnScreen(partListLocation)
      //partListView.animate().alpha(0f).start()
      melodyReferenceHolder.onPositionChanged()
      partListTransitionView.apply {
        alpha = 1f
        translationX = 0f
        translationY = 0f
        layoutWidth = partListView.width
        layoutHeight = partListView.height
        val targetTranslateX = nameLocation[0].toFloat() - partListLocation[0]
        val targetTranslateY = nameLocation[1].toFloat() - partListLocation[1]

        melodyView.alpha = 0f
        melodyView.translationX = melodyView.width.toFloat()
        animateWidth(name.width)
        animateHeight(name.height)
        animate().translationXY(targetTranslateX, targetTranslateY)//.alpha(0f)
          .withEndAction {
            animate().alpha(0f).withEndAction {
              layoutWidth = 0
              translationX = 0f
              translationY = 0f
            }.start()
          }.start()
      }
    } ?: hideMelodyViewBoring()
  }

  private fun hideMelodyViewBoring() {
    melodyView.animate()
      .translationX(melodyView.width.toFloat())
      .alpha(0f)
      .withEndAction { melodyView.translationX = 10.27f * melodyView.width }
      .start()
    partListView.animate().alpha(1f)
    partListTransitionView.apply {
      translationX = 0f
      translationY = 0f
      layoutWidth = 0
    }
  }

  fun showOrbifold(animated: Boolean = true) {
    if(orbifold.isHidden) {
      backStack.push {
        if (!orbifold.isHidden) {
          hideOrbifold()
          true
        } else false
      }
    }
    paletteToolbar.orbifoldButton.backgroundResource = R.drawable.toolbar_button_active_instrument
    paletteToolbar.updateInstrumentButtonPaddings()
    orbifold.conditionallyAnimateToSelectionState()
    orbifold.show(
      animation = if (orbifold.context.configuration.portrait) {
        HideAnimation.VERTICAL
      } else HideAnimation.HORIZONTAL,
      animated = animated,
      endAction = {
        orbifold.conditionallyAnimateToSelectionState()
      }
    )
  }

  fun hideOrbifold(animated: Boolean = true) {
    paletteToolbar.orbifoldButton.backgroundResource = R.drawable.toolbar_button
    paletteToolbar.updateInstrumentButtonPaddings()
    harmonyViewModel.isChoosingHarmonyChord = false
    harmonyViewModel.selectedHarmonyElements = null
    orbifold.hide(
      animation = if (orbifold.context.configuration.portrait) {
        HideAnimation.VERTICAL
      } else HideAnimation.HORIZONTAL,
      animated = animated
    )
  }

  fun showKeyboard(animated: Boolean = true) {
    backStack.push {
      if(!keyboardView.isHidden) {
        hideKeyboard()
        true
      } else false
    }
    keyboardView.show(animated)
    paletteToolbar.keysButton.backgroundResource = R.drawable.toolbar_button_active_instrument
    paletteToolbar.updateInstrumentButtonPaddings()

  }

  fun hideKeyboard(animated: Boolean = true) {
    paletteToolbar.keysButton.backgroundResource = R.drawable.toolbar_button
    paletteToolbar.updateInstrumentButtonPaddings()
    orbifold.customChordMode = false
    keyboardView.hide(animated)
  }

  fun showColorboard(animated: Boolean = true) {
    backStack.push {
      if(!colorboardView.isHidden) {
        hideColorboard()
        true
      } else false
    }
    colorboardView.show(animated)
    paletteToolbar.colorsButton.backgroundResource = R.drawable.toolbar_button_active_instrument
    paletteToolbar.updateInstrumentButtonPaddings()
  }

  fun hideColorboard(animated: Boolean = true) {
    paletteToolbar.colorsButton.backgroundResource = R.drawable.toolbar_button
    paletteToolbar.updateInstrumentButtonPaddings()
    colorboardView.hide(animated)
  }

  fun updateMelodyReferences() {
    partListAdapters.forEach {  partAdapter ->
      partAdapter.boundViewHolders.forEach { partHolder ->
        partHolder.melodyReferenceAdapter.boundViewHolders.forEach { melodyHolder ->
          melodyHolder.onPositionChanged()
        }
      }
    }
  }
}