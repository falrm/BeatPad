package com.jonlatane.beatpad.view.topology

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.harmony.ChordSequence
import com.jonlatane.beatpad.harmony.chord.Chord
import kotlin.properties.Delegates.observable

import java.util.ArrayList

import com.jonlatane.beatpad.harmony.CHROMATIC
import com.jonlatane.beatpad.harmony.chord.MAJ_7

/**
 * Created by jonlatane on 5/5/17.
 */
class TopologyView : RelativeLayout {
    var onChordChangedListener: ((Chord) -> Unit)? by observable<((Chord) -> Unit)?>(null) {
        _, _, listener ->
        listener?.invoke(chord)
    }
    var chord: Chord by observable(Chord(0, MAJ_7)) { _, _, chord ->
        if (selectedChord != null) {
            NavigationAnimations.animateToTargetChord(this)
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
                if (!forward.getText().equals(centralChord.text)) {
                    selectedChord = v as TextView
                    chord = sequence.forward(chord)
                }
            }
            forward.setText(sequence.forward(chord).name)
            back.setOnClickListener { v: View ->
                if (!back.getText().equals(centralChord.text)) {
                    selectedChord = v as TextView
                    chord = sequence.back(chord)
                }
            }
            back.setText(sequence.back(chord).name)
        }
    }

    interface OnChordChangedListener {
        fun onChordChanged(c: Chord)
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
            NavigationAnimations.skipToInitialState(this@TopologyView)
            NavigationAnimations.animateToSelectionPhase(this@TopologyView)
        }
    }

    internal fun updateChordText() {
        centralChord.setText(chord.name)
        halfStepUp.setText(CHROMATIC.forward(chord).name)
        halfStepDown.setText(CHROMATIC.back(chord).name)
        for (sv in sequences) {
            sv.forward.setText(sv.sequence.forward(chord).name)
            sv.back.setText(sv.sequence.back(chord).name)
        }
    }

    private fun inflateChordView(defaultZ: Float = 2f): TextView {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_chord, this, true)
        val result = findViewWithTag("newChord") as TextView
        result.setTag(null)
        result.setZ(defaultZ)
        return result
    }

    private fun inflateAxisView(): ImageView {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_axis, this, true)
        val result = findViewWithTag("newConnector") as ImageView
        result.setZ(0f)
        result.setTag(null)
        return result
    }

    private fun inflateConnectorView(): ImageView {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_connector, this, true)
        val result = findViewWithTag("newConnector") as ImageView
        result.setZ(CONNECTOR_Z)
        result.setTag(null)
        return result
    }

    private fun inflateBG() {
        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg, this, true)
        centralChordBackground = findViewWithTag("newBG") as ImageView
        centralChordBackground.setZ(5f)
        centralChordBackground.setTag(null)

        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg_half_steps, this, true)
        halfStepBackground = findViewWithTag("newBG") as ImageView
        halfStepBackground.setZ(3f)
        halfStepBackground.setTag(null)

        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg_highlight, this, true)
        centralChordThrobber = findViewWithTag("newBG") as ImageView
        centralChordThrobber.setZ(6f)
        centralChordThrobber.setTag(null)
        centralChordThrobber.setAlpha(0f)

        LayoutInflater.from(getContext()).inflate(R.layout.topology_bg, this, true)
        centralChordTouchPoint = findViewWithTag("newBG") as ImageView
        centralChordTouchPoint.setZ(1000f)
        centralChordTouchPoint.setAlpha(0f)
        centralChordTouchPoint.setTag(null)
    }

    fun onResume() {
        NavigationAnimations.animateToSelectionPhase(this)
    }

    fun addSequence(index: Int, sequence: ChordSequence) {
        if (!containsSequence(sequence)) {
            sequences.add(index, SequenceViews(sequence))
            NavigationAnimations.animateToSelectionPhase(this)
        }
    }

    fun addSequence(sequence: ChordSequence) {
        addSequence(sequences.size, sequence)
    }

    fun removeSequence(sequence: ChordSequence) {
        for (index in 0..sequences.size - 1) {
            val views = sequences[index]
            if (views.sequence === sequence) {
                animateViewOut(views.axis)
                animateViewOut(views.forward)
                animateViewOut(views.back)
                animateViewOut(views.connectForward)
                animateViewOut(views.connectBack)
                sequences.removeAt(index)
                NavigationAnimations.animateToSelectionPhase(this)
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
        for (index in 0..sequences.size - 1) {
            if (sequences[index].sequence === sequence) {
                return true
            }
        }
        return false
    }
    companion object {
        private val TAG = TopologyView::class.simpleName
    }
}
