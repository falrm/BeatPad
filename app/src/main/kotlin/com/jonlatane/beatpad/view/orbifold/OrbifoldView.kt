package com.jonlatane.beatpad.view.orbifold

import BeatClockPaletteConsumer.viewModel
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.orbifold.Orbifold
import com.jonlatane.beatpad.model.orbifold.Orbit
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.chord.Maj13
import com.jonlatane.beatpad.model.orbifold.orbit.Chromatic
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.showOrbifoldPicker
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.util.isHidden
import com.jonlatane.beatpad.view.HideableRelativeLayout
import com.jonlatane.beatpad.view.keyboard.KeyboardView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*
import kotlin.properties.Delegates.observable

class OrbifoldView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : HideableRelativeLayout(context) {
  var onChordChangedListener: ((Chord) -> Unit)? by observable<((Chord) -> Unit)?>(null) { _, _, listener ->
    listener?.invoke(chord)
  }
  var onOrbifoldChangeListener: ((Orbifold) -> Unit)? by observable<((Orbifold) -> Unit)?>(null) { _, _, listener ->
    listener?.invoke(orbifold)
  }
  var chord: Chord by observable(Chord(0, Maj13)) { _, _, chord ->
    if (selectedChord != null) {
      animateTo(InitialState)
    } else {
      updateChordText()
      post { conditionallyAnimateToSelectionState() }
    }
    onChordChangedListener?.invoke(chord)
  }
  var orbifold: Orbifold by observable(Orbifold.intermediate) { _, _, new ->
    (Orbifold.ALL_ORBITS - new).forEach {
      removeSequence(it)
    }
    new.indices.forEach {
      addSequence(it, new[it])
    }
    onOrbifoldChangeListener?.invoke(new)
  }
  var keyboard: KeyboardView? = null
  internal val customButton: Button
  internal val slashButton: Button
  internal val modeButton: Button
  internal var centralChord: TextView
  internal lateinit var centralChordBackground: View
  internal lateinit var centralChordThrobber: View
  internal lateinit var centralChordTouchPoint: View
  internal var halfStepUp: TextView
  internal var halfStepDown: TextView
  internal lateinit var halfStepBackground: View
  internal var selectedChord: TextView? = null
  internal var sequences: MutableList<SequenceViews> = ArrayList()
  private var drumPartBeforeCustomMode: Part? = null
  private var orbifoldBeforeCustomMode: Orbifold? = null
  var canEditChords: Boolean = false
  set(value) {
    field = value
    if(value) {
      listOf(customButton, modeButton, slashButton).forEach {
        it.isEnabled = true
        it.animate().alpha(1f)
      }
    } else {
      //TODO: Make this work with animateTo
      selectedChord = null
      animateTo(InitialState)
      listOf(customButton, modeButton, slashButton).forEach {
        it.isEnabled = false
        it.animate().alpha(0f)
      }
    }
  }
  var keyboardWasShowingBeforeCustomMode: Boolean = true
  var customChordMode: Boolean = false
  set(value) {
    field = value
    customButton.text = if(value) "Done" else "Custom"
    if(value) {
      orbifoldBeforeCustomMode = orbifold
      keyboardWasShowingBeforeCustomMode = keyboard?.isHidden == false
      this@OrbifoldView.orbifold = Orbifold.custom
      if(keyboard?.isHidden == true) {
        keyboard?.show()
      }
      keyboard?.ioHandler?.onEstablishedChordChanged = { notes ->
        disableNextTransitionAnimation()
        chord = Chord(chord.root, notes.map { it - chord.root }.toIntArray())
      }
      drumPartBeforeCustomMode = viewModel?.keyboardPart
        ?.takeIf { (it.instrument as? MIDIInstrument)?.drumTrack == true }
        ?.also {
          viewModel?.palette?.parts
            ?.first { (it.instrument as? MIDIInstrument)?.drumTrack == false }
            ?.let { firstNonDrumPart ->
              viewModel?.keyboardPart = firstNonDrumPart
              viewModel?.keyboardView?.ioHandler?.highlightChord(chord)
            }
        }

      viewModel?.backStack?.push {
        if (customChordMode) {
          customChordMode = false
          true
        } else false
      }
    } else {
      orbifoldBeforeCustomMode?.let { orbifold = it }
      drumPartBeforeCustomMode?.let {
        viewModel?.keyboardPart = it
        viewModel?.keyboardView?.ioHandler?.highlightChord(null)
      }
      if(!keyboardWasShowingBeforeCustomMode) {
        keyboard?.hide()
      }
      orbifoldBeforeCustomMode = null
      keyboard?.ioHandler?.onEstablishedChordChanged = null
    }
  }

  internal inner class SequenceViews(val sequence: Orbit) {
    val axis = inflateAxisView().apply { alpha = 0f }
    val connectForward = inflateConnectorView().apply { alpha = 0f }
    val connectBack = inflateConnectorView().apply { alpha = 0f }
    val forward = inflateChordView().apply { alpha = 0f }
    val back = inflateChordView().apply { alpha = 0f }

    init {
      forward.setOnClickListener { v: View ->
        if (forward.text != centralChord.text) {
          selectedChord = v as TextView
          chord = sequence.forward(chord)
        }
      }
      forward.text = sequence.forward(chord).name
      back.setOnClickListener { v: View ->
        if (back.text != centralChord.text) {
          selectedChord = v as TextView
          chord = sequence.back(chord)
        }
      }
      back.text = sequence.back(chord).name
    }
  }

  init {
    clipToPadding = false
    clipChildren = false
    centralChord = inflateChordView(centralChordElevation)
    halfStepUp = inflateChordView(halfStepChordElevation).apply { alpha = 0f }
    halfStepDown = inflateChordView(halfStepChordElevation).apply { alpha = 0f }
    halfStepUp.setOnClickListener { v: View ->
      selectedChord = v as TextView
      chord = Chromatic.forward(chord)
    }
    halfStepDown.setOnClickListener { v: View ->
      selectedChord = v as TextView
      chord = Chromatic.back(chord)
    }
    inflateBG()
    updateChordText()
    post {
      skipTo(InitialState)
      this.orbifold = orbifold
    }
    modeButton = button {
      text = "Mode"
      isEnabled = false
      alpha = 0f
      typeface = MainApplication.chordTypefaceBold
      onClick {
        showOrbifoldPicker(this@OrbifoldView)
      }
    }.lparams {
      alignParentBottom()
      alignParentRight()
      elevation = 1f
    }
    customButton = button {
      text = "Custom"
      isEnabled = false
      alpha = 0f
      typeface = MainApplication.chordTypefaceBold
      onClick {
        customChordMode = ! customChordMode
      }
    }.lparams {
      alignParentBottom()
      alignParentLeft()
      elevation = 1f
    }
    slashButton = button {
      text = "/"
      textScaleX = 5f
      isEnabled = false
      alpha = 0f
      typeface = MainApplication.chordTypefaceBold
      onClick {
        //showOrbifoldPicker(this@OrbifoldView)
      }
    }.lparams {
      alignParentTop()
      alignParentRight()
      elevation = 1f
    }

    backgroundColor = context.color(R.color.colorPrimaryDark)
  }

  internal fun updateChordText() {
    fun TextView.chordify(c: Chord) {
      text = c.name
      val p = arrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
      /*backgroundResource = when {
        c.isDominant -> R.drawable.orbifold_chord_dominant
        c.isDiminished -> R.drawable.orbifold_chord_diminished
        c.isMinor -> R.drawable.orbifold_chord_minor
        c.isAugmented -> R.drawable.orbifold_chord_augmented
        c.isMajor -> R.drawable.orbifold_chord_major
        else -> R.drawable.orbifold_chord
      }*/
      textColor = when {
        c.isDominant   -> color(R.color.dominant)
        c.isDiminished -> color(R.color.diminished)
        c.isMinor      -> color(R.color.minor)
        c.isAugmented  -> color(R.color.augmented)
        c.isMajor      -> color(R.color.major)
        else           -> color(android.R.color.white)
      }
      setPadding(p[0], p[1], p[2], p[3])
    }

    centralChord.chordify(chord)
    halfStepUp.chordify(Chromatic.forward(chord))
    halfStepDown.chordify(Chromatic.back(chord))
    for (sv in sequences) {
      sv.forward.chordify(sv.sequence.forward(chord))
      sv.back.chordify(sv.sequence.back(chord))
    }
  }

  private fun inflateChordView(defaultElevation: Float = defaultChordElevation): TextView {
    LayoutInflater.from(context).inflate(R.layout.orbifold_chord, this, true)
    val result = findViewWithTag<TextView>("newChord").apply {
      elevation = defaultElevation
      tag = null
    } as TextView
    return result.apply {
      typeface = MainApplication.chordTypeface
    }
  }

  private fun inflateAxisView(): View {
    LayoutInflater.from(context).inflate(R.layout.orbifold_axis, this, true)
    val result = findViewWithTag<View>("newConnector").apply {
      elevation = axisElevation
      tag = null
    }
    return result
  }

  private fun inflateConnectorView(): View {
    LayoutInflater.from(context).inflate(R.layout.orbifold_connector, this, true)
    val result = findViewWithTag<View>("newConnector").apply {
      elevation = connectorElevation
      tag = null
    }
    return result
  }

  private fun inflateBG() {
    LayoutInflater.from(context).inflate(R.layout.orbifold_bg_half_steps, this, true)
    halfStepBackground = findViewWithTag<View>("newBG").apply {
      elevation = halfStepBackgroundElevation
      tag = null
    }

    LayoutInflater.from(context).inflate(R.layout.orbifold_bg_highlight, this, true)
    centralChordThrobber = findViewWithTag<View>("newBG").apply {
      outlineProvider = null
      elevation = Float.MAX_VALUE - 1f
      tag = null
      alpha = 0f
    }

    centralChordBackground = view {
      id = R.id.orbifold_background
      //outlineProvider = null
      //tag = null
      background = context.getDrawable(R.drawable.orbifold_bg_highlight)
      elevation = centralBackgroundElevation
    }.lparams(dip(180), height = dip(108)) {
      centerInParent()
    }

    LayoutInflater.from(context).inflate(R.layout.orbifold_bg_highlight, this, true)
    centralChordTouchPoint = findViewWithTag<View>("newBG").apply {
      elevation = Float.MAX_VALUE
      alpha = 0f
      tag = null
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    post { conditionallyAnimateToSelectionState() }
  }

  fun onResume() {
    conditionallyAnimateToSelectionState()
    //animateTo(SelectionState)
  }

  private fun addSequence(index: Int, sequence: Orbit) {
    if (!containsSequence(sequence)) {
      sequences.add(index, SequenceViews(sequence))
      updateChordText()
      post { conditionallyAnimateToSelectionState() }
    }
  }

  private fun removeSequence(sequence: Orbit) {
    for (index in 0 until sequences.size) {
      val views = sequences[index]
      if (views.sequence === sequence) {
        animateViewOut(views.axis)
        animateViewOut(views.forward)
        animateViewOut(views.back)
        animateViewOut(views.connectForward)
        animateViewOut(views.connectBack)
        sequences.removeAt(index)
        conditionallyAnimateToSelectionState()
        break
      }
    }
  }

  private fun animateViewOut(child: View) {
    child.animate().alpha(0f).withEndAction {
      this@OrbifoldView.removeView(child)
    }
  }

  fun containsSequence(sequence: Orbit): Boolean {
    return (0..sequences.size - 1).any { sequences[it].sequence === sequence }
  }

  fun prepareAnimationTo(chord: Chord) {
    val view: TextView? = sequences.flatMap {
      listOf(it.sequence.forward(chord) to it.forward, it.sequence.back(chord) to it.back)
    }.firstOrNull {
      it.first == chord
    }?.second
    selectedChord = view
  }

  fun skipTo(state: NavigationState) = state.skipTo(this)
  fun animateTo(state: NavigationState) = state.animateTo(this)
  fun conditionallyAnimateToSelectionState() {
    if(canEditChords) animateTo(SelectionState)
    else animateTo(InitialState)
  }
  fun disableNextTransitionAnimation() {
    selectedChord = null
  }
}
