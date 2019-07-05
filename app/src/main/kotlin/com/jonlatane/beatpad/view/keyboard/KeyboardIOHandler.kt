package com.jonlatane.beatpad.view.keyboard

import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.jonlatane.beatpad.R
import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.util.mod12
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.verbose
import java.util.*
import kotlin.properties.Delegates.observable

class KeyboardIOHandler(
	private val keyboardView: KeyboardView
): AnkoLogger {
	var instrument by observable<Instrument>(MIDIInstrument()) { _, old, _ -> old.stop() }
	private val currentlyPressed = Collections.synchronizedSet(HashSet<Int>())
	private val _establishedChord = mutableSetOf<Int>()
	val establishedChord get() = _establishedChord.toSet()
  interface EstablishedChordChangedListener: ((Set<Int>) -> Unit)
  var onEstablishedChordChanged: ((Set<Int>) -> Unit)? = null

	init {
		KEY_IDS
			.map {
				keyboardView.findViewById<Button>(it).apply {
					setOnTouchListener { touchedKey, event ->
						catchRogues()
						var result = false
						if (event.actionMasked == MotionEvent.ACTION_DOWN) {
							pressNote(KEY_IDS_INVERSE.get(touchedKey.id))
						} else if (event.actionMasked == MotionEvent.ACTION_UP) {
							liftNote(KEY_IDS_INVERSE.get(touchedKey.id))
						} else if (event.actionMasked == MotionEvent.ACTION_MOVE
							&& event.pointerCount != 1
							&& currentlyPressed.size > 1) {
							result = true
						}
						result
					}
				}

				// Make sure we don't get stuck keys
			}
			.map { it.viewTreeObserver }
			.forEach {
				it.addOnPreDrawListener {
					//catchRogues()
					true
				}
			}
	}

	/**
	 * Highlights the given chord on the keyboard
	 * @param harmonicChord
	 */
	fun highlightChord(harmonicChord: Chord?) {
		if (harmonicChord != null) {
			verbose("Highlighting chord ${harmonicChord.name}")
			for (id in KEY_IDS) {
				val b = keyboardView.findViewById<Button>(id)
				val tone = KEY_IDS_INVERSE.get(id)
				val toneClass = (1200 + tone) % 12
				val isInChord = harmonicChord.containsTone(toneClass)
				if (isInChord) {
					b.backgroundResource = when ((tone - harmonicChord.root).mod12) {
						0 -> R.drawable.key_standard_tonic
						1 -> R.drawable.key_standard_2_flat
						2 -> R.drawable.key_standard_2
						3 -> R.drawable.key_standard_3_flat
						4 -> R.drawable.key_standard_3
						5 -> R.drawable.key_standard_4
						6 -> R.drawable.key_standard_5_flat
						7 -> R.drawable.key_standard_5
						8 -> R.drawable.key_standard_5_sharp
						9 -> R.drawable.key_standard_6
						10 -> R.drawable.key_standard_7_flat
						11 -> R.drawable.key_standard_7
						else -> throw IllegalStateException()
					}
				} else {
					if (isBlackKey(tone)) {
						b.backgroundResource = R.drawable.key_standard_black
					} else {
						b.backgroundResource = R.drawable.key_standard_white
					}
				}
			}
		} else {
			verbose("Clearing highlights")
			for (id in KEY_IDS) {
				val n = KEY_IDS_INVERSE.get(id)
				if (isBlackKey(n)) {
					keyboardView.findViewById<View>(id).setBackgroundResource(R.drawable.key_standard_black)
				} else {
					keyboardView.findViewById<View>(id).setBackgroundResource(R.drawable.key_standard_white)
				}
			}
		}
	}

	private fun isBlackKey(note: Int): Boolean {
		return when(note.mod12) {
			1, 3, 6, 8, 10 -> true
			else -> false
		}
		//return noteClass == 1 || noteClass == 3 || noteClass == 6 || noteClass == 8 || noteClass == 10
	}

	private fun liftNote(n: Int) {
		synchronized(currentlyPressed) {
			currentlyPressed.remove(n)
			if(currentlyPressed.isEmpty()) {
				_establishedChord.clear()
			}
		}
		instrument.stop(n)
	}

	private fun pressNote(n: Int) {
		synchronized(currentlyPressed) {
			currentlyPressed.add(n)
			if(_establishedChord.add(n.mod12)) {
        onEstablishedChordChanged?.invoke(establishedChord)
      }
		}
		instrument.play(n, 127)
	}

	// Utility method in case the keyboard is scrolled when keys are pressed.
	private fun catchRogues() {
		val iterator = currentlyPressed.iterator()
		while (iterator.hasNext()) {
			val n = iterator.next()
			val b = keyboardView.findViewById<Button>(KEY_IDS[n + 39])
			if (!b.isPressed) {
				iterator.remove()
				liftNote(n)
			}
		}
	}

	companion object {
		private val KEY_IDS = intArrayOf(R.id.keyA0, R.id.keyAS0, R.id.keyB0, R.id.keyC1, R.id.keyCS1, R.id.keyD1, R.id.keyDS1, R.id.keyE1, R.id.keyF1, R.id.keyFS1, R.id.keyG1, R.id.keyGS1, R.id.keyA1, R.id.keyAS1, R.id.keyB1, R.id.keyC2, R.id.keyCS2, R.id.keyD2, R.id.keyDS2, R.id.keyE2, R.id.keyF2, R.id.keyFS2, R.id.keyG2, R.id.keyGS2, R.id.keyA2, R.id.keyAS2, R.id.keyB2, R.id.keyC3, R.id.keyCS3, R.id.keyD3, R.id.keyDS3, R.id.keyE3, R.id.keyF3, R.id.keyFS3, R.id.keyG3, R.id.keyGS3, R.id.keyA3, R.id.keyAS3, R.id.keyB3, R.id.keyC4, R.id.keyCS4, R.id.keyD4, R.id.keyDS4, R.id.keyE4, R.id.keyF4, R.id.keyFS4, R.id.keyG4, R.id.keyGS4, R.id.keyA4, R.id.keyAS4, R.id.keyB4, R.id.keyC5, R.id.keyCS5, R.id.keyD5, R.id.keyDS5, R.id.keyE5, R.id.keyF5, R.id.keyFS5, R.id.keyG5, R.id.keyGS5, R.id.keyA5, R.id.keyAS5, R.id.keyB5, R.id.keyC6, R.id.keyCS6, R.id.keyD6, R.id.keyDS6, R.id.keyE6, R.id.keyF6, R.id.keyFS6, R.id.keyG6, R.id.keyGS6, R.id.keyA6, R.id.keyAS6, R.id.keyB6, R.id.keyC7, R.id.keyCS7, R.id.keyD7, R.id.keyDS7, R.id.keyE7, R.id.keyF7, R.id.keyFS7, R.id.keyG7, R.id.keyGS7, R.id.keyA7, R.id.keyAS7, R.id.keyB7, R.id.keyC8)
		private val KEY_IDS_INVERSE = SparseIntArray()

		init {
			var i = 0
			while (i < KEY_IDS.size) {
				KEY_IDS_INVERSE.put(KEY_IDS[i], i - 39)
				i += 1
			}
		}
	}
}
