package com.jonlatane.beatpad.view.topology

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.Topology
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.Maj7
import com.jonlatane.beatpad.harmony.chordsequence.Chromatic
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import java.util.*
import kotlin.properties.Delegates.observable

class TopologyView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {
	var onChordChangedListener: ((Chord) -> Unit)? by observable<((Chord) -> Unit)?>(null) {
		_, _, listener ->
		listener?.invoke(chord)
	}
	var chord: Chord by observable(Chord(0, Maj7)) { _, _, chord ->
		if (selectedChord != null) {
			animateTo(InitialState)
		} else {
			updateChordText()
		}
		onChordChangedListener?.invoke(chord)
	}
	var topology: Topology by observable(Topology.intermediate) {
		_, _, new ->
		(Topology.allSequences - new).forEach {
			removeSequence(it)
		}
		new.indices.forEach {
			addSequence(it, new[it])
		}
	}
	internal var centralChord: TextView
	internal lateinit var centralChordThrobber: View
	internal lateinit var centralChordTouchPoint: View
	internal var halfStepUp: TextView
	internal var halfStepDown: TextView
	internal lateinit var halfStepBackground: View
	internal var selectedChord: TextView? = null
	internal var sequences: MutableList<SequenceViews> = ArrayList()

	internal inner class SequenceViews(val sequence: ChordSequence) {
		val axis = inflateAxisView()
		val connectForward = inflateConnectorView()
		val connectBack = inflateConnectorView()
		val forward = inflateChordView()
		val back = inflateChordView()

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
		backgroundColor = R.color.brown
		clipToPadding = false
		clipChildren = false
		centralChord = inflateChordView(centralChordElevation)
		halfStepUp = inflateChordView(halfStepChordElevation)
		halfStepDown = inflateChordView(halfStepChordElevation)
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
			this.topology = topology
			animateTo(SelectionState)
		}
	}

	internal fun updateChordText() {
		fun TextView.chordify(c: Chord) {
			text = c.name
			val p = arrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
			backgroundResource = when {
				c.isDominant -> R.drawable.topology_chord_dominant
				c.isDiminished -> R.drawable.topology_chord_diminished
				c.isMinor -> R.drawable.topology_chord_minor
				c.isAugmented -> R.drawable.topology_chord_augmented
				c.isMajor -> R.drawable.topology_chord_major
				else -> R.drawable.topology_chord
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
		LayoutInflater.from(context).inflate(R.layout.topology_chord, this, true)
		val result = findViewWithTag("newChord") as TextView
		result.tag = null
		result.elevation = defaultElevation
		return result
	}

	private fun inflateAxisView(): View {
		LayoutInflater.from(context).inflate(R.layout.topology_axis, this, true)
		val result = findViewWithTag("newConnector")
		result.elevation = axisElevation
		result.tag = null
		return result
	}

	private fun inflateConnectorView(): View {
		LayoutInflater.from(context).inflate(R.layout.topology_connector, this, true)
		val result = findViewWithTag("newConnector")
		result.elevation = connectorElevation
		result.tag = null
		return result
	}

	private fun inflateBG() {
		LayoutInflater.from(context).inflate(R.layout.topology_bg_half_steps, this, true)
		halfStepBackground = findViewWithTag("newBG")
		halfStepBackground.elevation = halfStepBackgroundElevation
		halfStepBackground.tag = null

		LayoutInflater.from(context).inflate(R.layout.topology_bg_highlight, this, true)
		centralChordThrobber = findViewWithTag("newBG")
		centralChordThrobber.outlineProvider = null
		centralChordThrobber.elevation = Float.MAX_VALUE
		centralChordThrobber.tag = null
		centralChordThrobber.alpha = 0f

		LayoutInflater.from(context).inflate(R.layout.topology_chord, this, true)
		centralChordTouchPoint = findViewWithTag("newChord")
		centralChordTouchPoint.elevation = Float.MAX_VALUE
		centralChordTouchPoint.alpha = 0f
		centralChordTouchPoint.tag = null
	}

	fun onResume() {
		animateTo(SelectionState)
	}

	private fun addSequence(index: Int, sequence: ChordSequence) {
		if (!containsSequence(sequence)) {
			sequences.add(index, SequenceViews(sequence))
			updateChordText()
			post { animateTo(SelectionState) }
		}
	}

	private fun removeSequence(sequence: ChordSequence) {
		for (index in 0..sequences.size - 1) {
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
			this@TopologyView.removeView(child)
		}
	}

	fun containsSequence(sequence: ChordSequence): Boolean {
		return (0..sequences.size - 1).any { sequences[it].sequence === sequence }
	}

	fun skipTo(state: NavigationState) = state.skipTo(this)
	fun animateTo(state: NavigationState) = state.animateTo(this)
}
