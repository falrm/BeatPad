package com.jonlatane.beatpad.view.topology

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.harmony.CHROMATIC
import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.Topology
import com.jonlatane.beatpad.harmony.chord.Chord
import com.jonlatane.beatpad.harmony.chord.MAJ_7
import java.util.*
import kotlin.properties.Delegates.observable

/**
 * Created by jonlatane on 5/5/17.
 */
class TopologyView : RelativeLayout, Topology.TopologyViewer {
    var onChordChangedListener: ((Chord) -> Unit)? by observable<((Chord) -> Unit)?>(null) {
        _, _, listener ->
        listener?.invoke(chord)
    }
    var chord: Chord by observable(Chord(0, MAJ_7)) { _, _, chord ->
        if (selectedChord != null) {
            animateTo(InitialState)
        } else {
            updateChordText()
        }
        onChordChangedListener?.invoke(chord)
    }
    internal lateinit var centralChord: TextView
    internal lateinit var centralChordBackground: ImageView
    internal lateinit var centralChordThrobber: ImageView
    internal lateinit var centralChordTouchPoint: ImageView
    internal lateinit var halfStepUp: TextView
    internal lateinit var halfStepDown: TextView
    internal lateinit var halfStepBackground: ImageView
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

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        centralChord = inflateChordView(7f)
        halfStepUp = inflateChordView(4f)
        halfStepDown = inflateChordView(4f)
        halfStepUp.setOnClickListener { v: View ->
            selectedChord = v as TextView
            chord = CHROMATIC.forward(chord)
        }
        halfStepDown.setOnClickListener { v: View ->
            selectedChord = v as TextView
            chord = CHROMATIC.back(chord)
        }
        inflateBG()
        updateChordText()
        post {
            skipTo(InitialState)
            animateTo(SelectionState)
        }
    }

    internal fun updateChordText() {
        centralChord.text = chord.name
        halfStepUp.text = CHROMATIC.forward(chord).name
        halfStepDown.text = CHROMATIC.back(chord).name
        for (sv in sequences) {
            sv.forward.text = sv.sequence.forward(chord).name
            sv.back.text = sv.sequence.back(chord).name
        }
    }

    private fun inflateChordView(defaultZ: Float = 2f): TextView {
        LayoutInflater.from(context).inflate(R.layout.topology_chord, this, true)
        val result = findViewWithTag("newChord") as TextView
        result.tag = null
        result.z = defaultZ
        return result
    }

    private fun inflateAxisView(): ImageView {
        LayoutInflater.from(context).inflate(R.layout.topology_axis, this, true)
        val result = findViewWithTag("newConnector") as ImageView
        result.z = 0f
        result.tag = null
        return result
    }

    private fun inflateConnectorView(): ImageView {
        LayoutInflater.from(context).inflate(R.layout.topology_connector, this, true)
        val result = findViewWithTag("newConnector") as ImageView
        result.z = CONNECTOR_Z
        result.tag = null
        return result
    }

    private fun inflateBG() {
        LayoutInflater.from(context).inflate(R.layout.topology_bg, this, true)
        centralChordBackground = findViewWithTag("newBG") as ImageView
        centralChordBackground.z = 5f
        centralChordBackground.tag = null

        LayoutInflater.from(context).inflate(R.layout.topology_bg_half_steps, this, true)
        halfStepBackground = findViewWithTag("newBG") as ImageView
        halfStepBackground.z = 3f
        halfStepBackground.tag = null

        LayoutInflater.from(context).inflate(R.layout.topology_bg_highlight, this, true)
        centralChordThrobber = findViewWithTag("newBG") as ImageView
        centralChordThrobber.z = 6f
        centralChordThrobber.tag = null
        centralChordThrobber.alpha = 0f

        LayoutInflater.from(context).inflate(R.layout.topology_bg, this, true)
        centralChordTouchPoint = findViewWithTag("newBG") as ImageView
        centralChordTouchPoint.z = 1000f
        centralChordTouchPoint.alpha = 0f
        centralChordTouchPoint.tag = null
    }

    fun onResume() {
        animateTo(SelectionState)
    }

    override fun addSequence(index: Int, sequence: ChordSequence) {
        if (!containsSequence(sequence)) {
            sequences.add(index, SequenceViews(sequence))
            updateChordText()
            post { animateTo(SelectionState) }
        }
    }

    override fun removeSequence(sequence: ChordSequence) {
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
}
