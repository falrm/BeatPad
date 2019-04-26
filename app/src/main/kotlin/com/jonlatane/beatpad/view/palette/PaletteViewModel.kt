package com.jonlatane.beatpad.view.palette

//import com.jonlatane.beatpad.util.syncPositionTo
import BeatClockPaletteConsumer
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.*
import com.jonlatane.beatpad.view.colorboard.ColorboardInputView
import com.jonlatane.beatpad.view.harmony.HarmonyView
import com.jonlatane.beatpad.view.harmony.HarmonyViewModel
import com.jonlatane.beatpad.view.keyboard.KeyboardView
import com.jonlatane.beatpad.view.melody.MelodyViewModel
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import com.jonlatane.beatpad.view.orbifold.RhythmAnimations
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.configuration
import org.jetbrains.anko.portrait
import kotlin.properties.Delegates.observable

/**
 * The PaletteViewModel still assumes we'll only be editing
 * one Melody at a time.
 */
class PaletteViewModel: AnkoLogger {
  init {
    //BeatClockPaletteConsumer.viewModel = this
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
  var melodyView
    get() = melodyViewModel.melodyView
    set(value) {
      melodyViewModel.melodyView = value
    }
  var melodyBeatAdapter
    get() = melodyViewModel.beatAdapter
    set(value) {
      melodyViewModel.beatAdapter = value
    }

  val harmonyViewModel = HarmonyViewModel()
    .apply { paletteViewModel = this@PaletteViewModel }
  var harmonyView: HarmonyView
    get() = harmonyViewModel.harmonyView!!
    set(value) { harmonyViewModel.harmonyView = value }

  var wasOrbifoldShowingBeforeEditingChord: Boolean? = null
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
    toolbarView.updateTempoButton()
    partListAdapter?.notifyDataSetChanged()
    sectionListAdapter?.notifyDataSetChanged()
    if(!new.sections.contains(BeatClockPaletteConsumer.section)) {
      BeatClockPaletteConsumer.section = new.sections.first()
    }
  }

  var editingMix by observable(false) { _, _, editingVolume ->
    partListAdapter?.boundViewHolders
      ?.forEach {
//        info("Configuring volume for ${it.partName.text}")
//        partListAdapter?.recyclerView?.post {
          it.editingVolume = editingVolume
//        }
      }
//    partListAdapter?.notifyDataSetChanged()
  }


  var editingMelody: Melody<*>? by observable<Melody<*>?>(null) { _, old, new ->
    if (new != null) {
      melodyViewModel.openedMelody = new
      colorboardView.hide()
      keyboardView.hide()
      harmonyViewModel.beatAdapter
        .syncPositionTo(melodyViewModel.melodyCenterHorizontalScroller)

      // Fancy animation of the thing if possible
       editMelodyMode()
    } else {
      partListMode(old)
    }
  }

  lateinit var sectionListView: View
  lateinit var sectionListRecycler: RecyclerView
  var partListAdapter: PartListAdapter? = null
  var sectionListAdapter: SectionListAdapter? = null
  lateinit var partListView: PartListView
  lateinit var partListTransitionView: TextView
  lateinit var toolbarView: PaletteToolbar
  lateinit var keyboardView: KeyboardView
  lateinit var colorboardView: ColorboardInputView
  var keyboardPart by observable<Part?>(null) { _, _, new ->
    if (new != null) keyboardView.ioHandler.instrument = new.instrument
    palette.keyboardPart = new
    val keyboardDrumTrack = (new?.instrument as? MIDIInstrument)?.drumTrack == true
    if(keyboardDrumTrack) {
      keyboardView.ioHandler.highlightChord(null)
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

  fun onBackPressed(): Boolean = when {
    orbifold.customChordMode                -> {
      orbifold.customChordMode = false
      true
    }
    harmonyViewModel.isChoosingHarmonyChord -> {
      harmonyViewModel.isChoosingHarmonyChord = false
      harmonyViewModel.selectedHarmonyElements = null
      true
    }
    editingMelody != null                   -> {
      editingMelody = null
      true
    }
    editingMix                              -> {
      editingMix = false
      true
    }
    else                                    -> false
  }

//  fun onBackPressed(): Boolean {
//    val result = harmonyViewModel.isChoosingHarmonyChord || editingMix || editingMelody != null
//    if(harmonyViewModel.isChoosingHarmonyChord) {
//      harmonyViewModel.isChoosingHarmonyChord = false
//      harmonyViewModel.selectedHarmonyElements = null
//    } else if(editingMix) {
//      editingMix = false
//    } else {
//      editingMelody = null
//    }
//    return result
//  }

  fun notifySectionChange() {
    harmonyViewModel.apply {
      notifyHarmonyChanged()
      editingChord = null
      selectedHarmonyElements = null
    }

    sectionListAdapter?.notifyDataSetChanged()
    melodyViewModel.beatAdapter.notifyDataSetChanged()
    if(editingMix) { // Trigger an update of the mix state.
      editingMix = editingMix
    }
    if(
      BeatClockPaletteConsumer.section?.melodies?.filter { !it.isDisabled }
        ?.map { it.melody }?.contains(editingMelody) != true
    ) {
      editingMelody = null
    }
    partListAdapter?.notifyDataSetChanged()
  }

  private fun editMelodyMode() {
    partListView.viewHolders<PartHolder>().mapNotNull { partHolder ->
      partHolder.melodyRecycler.viewHolders<MelodyReferenceHolder>()
        .firstOrNull { it.melody == editingMelody }
    }/*.let { listOf<MelodyReferenceHolder?>(null) }*/.firstOrNull()?.let { melodyReferenceHolder ->
      melodyReferenceHolder.enableMelodyReference()
      melodyReferenceHolder.onPositionChanged()
      val name = melodyReferenceHolder.layout.name
      val partListLocation = intArrayOf(-1, -1)
      val nameLocation = intArrayOf(-1, -1)
      name.getLocationOnScreen(nameLocation)
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
          animateWidth(partListView.width)
          animateHeight(partListView.height + orbifold.height)
          animate().translationX(0f).translationY(0f)
            .withEndAction {
              melodyViewModel.melodyView.let { melodyView ->
                melodyView.alpha = 0f
                melodyView.translationX = 0f
                melodyView.animate().alpha(1f).withEndAction {
                }.start()
              }
            }.start()
        }
        //}.start()
      }
    } ?: editMelodyModeBoring()
  }

  private fun editMelodyModeBoring() {
    partListTransitionView.apply {
      translationX = partListView.width.toFloat()
      translationY = 0f
      layoutWidth = partListView.width
      layoutHeight = partListView.height
      animate().translationX(0f).start()
    }
    melodyViewModel.melodyView.animate()
      .translationX(0f)
      .alpha(1f)
      .start()
    partListView.animate().alpha(0f).start()
  }

  private fun partListMode(oldValue: Melody<*>?) {
    partListView.viewHolders<PartHolder>().mapNotNull { partHolder ->
      partHolder.melodyRecycler.viewHolders<MelodyReferenceHolder>()
        .firstOrNull { !it.isAddButton && it.melody == oldValue }
    }/*let { listOf<MelodyReferenceHolder?>(null) }.*/.firstOrNull()?.let { melodyReferenceHolder ->
      val name = melodyReferenceHolder.layout.name
      val partListLocation = intArrayOf(-1, -1)
      val nameLocation = intArrayOf(-1, -1)
      name.getLocationOnScreen(nameLocation)
      partListView.animate().alpha(1f)
      partListView.getLocationOnScreen(partListLocation)
      //partListView.animate().alpha(0f).start()
      partListTransitionView.apply {
        translationX = 0f
        translationY = 0f
        layoutWidth = partListView.width
        layoutHeight = partListView.height
        val targetTranslateX = nameLocation[0].toFloat() - partListLocation[0]
        val targetTranslateY = nameLocation[1].toFloat() - partListLocation[1]

        melodyView.alpha = 0f
        animateWidth(name.width)
        animateHeight(name.height)
        animate().translationX(targetTranslateX).translationY(targetTranslateY)//.alpha(0f)
          .withEndAction {
            animate().alpha(0f).withEndAction {
              layoutWidth = 0
              translationX = 0f
              translationY = 0f
            }.start()
          }.start()
      }
    } ?: partListModeBoring()
  }

  fun showOrbifold(animated: Boolean = true) {
    orbifold.show(
      animation = if (orbifold.context.configuration.portrait) {
        HideAnimation.VERTICAL
      } else HideAnimation.HORIZONTAL,
      animated = animated
    )

  }

  fun hideOrbifold(animated: Boolean = true) {
    orbifold.hide(
      animation = if (orbifold.context.configuration.portrait) {
        HideAnimation.VERTICAL
      } else HideAnimation.HORIZONTAL,
      animated = animated
    )
  }

  private fun partListModeBoring() {
    melodyView.animate()
      .translationX(melodyView.width.toFloat())
      .alpha(0f)
      .start()
    partListView.animate().alpha(1f)
    partListTransitionView.apply {
      translationX = 0f
      translationY = 0f
      layoutWidth = 0
    }
  }
}