package com.jonlatane.beatpad.view.orbifold

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.jonlatane.beatpad.MainApplication
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.harmony.Orbit
import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Maj13
import com.jonlatane.beatpad.model.harmony.chord.Maj7
import com.jonlatane.beatpad.model.harmony.chordsequence.Chromatic
import com.jonlatane.beatpad.showOrbifoldPicker
import com.jonlatane.beatpad.util.color
import com.jonlatane.beatpad.view.HideableRelativeLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*
import kotlin.properties.Delegates.observable

class OrbifoldView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : HideableRelativeLayout(context) {
  /*inline fun <T : View> T.lparams(
    width: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
    init: RelativeLayout.LayoutParams.() -> Unit
  ): T {
    val layoutParams = RelativeLayout.LayoutParams(width, height)
    layoutParams.init()
    this@lparams.layoutParams = layoutParams
    return this
  }*/

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
      post { animateTo(SelectionState) }
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
  internal var centralChord: TextView
  internal lateinit var centralChordBackground: View
  internal lateinit var centralChordThrobber: View
  internal lateinit var centralChordTouchPoint: View
  internal var halfStepUp: TextView
  internal var halfStepDown: TextView
  internal lateinit var halfStepBackground: View
  internal var selectedChord: TextView? = null
  internal var sequences: MutableList<SequenceViews> = ArrayList()

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
      animateTo(SelectionState)
    }
    val orbifold = this
    button {
      text = "Mode"
      onClick {
        showOrbifoldPicker(orbifold)
      }
    }.lparams {
      alignParentBottom()
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
    post { animateTo(SelectionState) }
  }

  fun onResume() {
    //animateTo(SelectionState)
  }

  private fun addSequence(index: Int, sequence: Orbit) {
    if (!containsSequence(sequence)) {
      sequences.add(index, SequenceViews(sequence))
      updateChordText()
      post { animateTo(SelectionState) }
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
        animateTo(SelectionState)
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

  fun skipTo(state: NavigationState) = state.skipTo(this)
  fun animateTo(state: NavigationState) = state.animateTo(this)
  fun disableNextTransitionAnimation() {
    selectedChord = null
  }
}
