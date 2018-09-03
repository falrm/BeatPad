package com.jonlatane.beatpad.model.harmony.chord

import android.os.Parcel
import android.os.Parcelable
import com.jonlatane.beatpad.BuildConfig
import com.jonlatane.beatpad.model.Transposable
import com.jonlatane.beatpad.model.harmony.chord.heptatonics.Heptatonics
import com.jonlatane.beatpad.util.mod12
import com.jonlatane.beatpad.view.colorboard.AlphaDrawer
import com.jonlatane.beatpad.view.colorboard.BaseColorboardView

class Chord : Parcelable, Transposable<Chord> {
	val root: Int
	val extension: IntArray
	val heptatonics: Heptatonics

	constructor(root: Int, extension: IntArray) {
		this.root = root.mod12
		this.extension = (extension + 0).map { it.mod12 }.toSet().let {
			if(BuildConfig.DEBUG) it.sorted() else it
		}.toIntArray()
		this.heptatonics = Heptatonics(setOf(*this.extension.toTypedArray()))
	}

	private constructor(parcel: Parcel) {
		root = parcel.readInt().mod12
		extension = IntArray(parcel.readInt())
		parcel.readIntArray(extension)
		heptatonics = Heptatonics(setOf(*extension.toTypedArray()))
	}

	fun plus(vararg newTones: Int): Chord {
		val newExtension = IntArray(extension.size + newTones.size)
		System.arraycopy(extension, 0, newExtension, 0, extension.size)
		System.arraycopy(newTones, 0, newExtension, extension.size, newTones.size)
		return Chord(root, newExtension)
	}

	/**
	 * Gets a derivative chord created by replacing all of the outTones with inTones.
	 * For instance, substituteIfPresent(2,1) will convert 9 chords to b9 chords, but
	 * leave triads, 7 chords, 11#9 and 13#9 chords untouched.
	 * @param outTone
	 * *
	 * @param inTone
	 * *
	 * @return A new chord.  Its extension will have all of this chord's instances of outTone replaced
	 * *         with inTone.  If none were found, inTone will be added.
	 */
	fun substituteIfPresent(outTone: Int, inTone: Int): Chord {
		var found = false
		val newExtension = (extension.map {
			if(it == outTone) {
				found = true
				inTone
			} else it
		}).toSet().toIntArray()
		var result = Chord(root, newExtension)
		if (!found) {
			result = result.plus(inTone)
		}
		return result
	}

	fun replaceOrAdd(outTone: Int, inTone: Int): Chord {
		var found = false
		val newExtension = (extension.map {
			if(it == outTone) {
				found = true
				inTone
			} else it
		} + inTone).toSet().toIntArray()
		var result = Chord(root, newExtension)
		if (!found) {
			result = result.plus(inTone)
		}
		return result
	}

	fun with(extra: Int) = Chord(root, extension + extra)

	fun with(vararg extras: Int) = Chord(root, extension + extras)

	fun conditionallyWith(vararg tone: Int, condition: Chord.() -> Boolean)
		= if(condition.invoke(this)) with(*tone) else this

	val autoP5 get() = conditionallyWith(P5) { heptatonics.fifth == NONEXISTENT }
	val autoM6 get() = substituteIfPresent(m6, M6)

	fun withThird(type: Int): Chord = when(type) {
		heptatonics.third -> this
		NONEXISTENT -> when(heptatonics.third) {
			MAJOR -> remove(M3)
			MINOR -> remove(m3)
			else -> this
		}
		MAJOR -> when(heptatonics.third) {
			MAJOR -> this
			MINOR -> remove(m3).with(M3)
			else -> with(M3)
		}
		MINOR -> when(heptatonics.third) {
			MAJOR -> remove(M3).with(m3)
			MINOR -> this
			else -> with(m3)
		}
		else -> TODO()
	}

	fun remove(tone: Int) = substituteIfPresent(tone, 0)

	fun changeRoot(interval: Int) = Chord(
		root = root + interval,
	  extension = extension.map { it - interval }.toIntArray()
	)

	override fun transpose(interval: Int) = Chord(
		root = root + interval,
		extension = extension
	)

	/**
	 * @param bottom lowest allowed note, inclusive
	 * @param top highest allowed note, inclusive
	 */
	fun getTones(bottom: Int = AlphaDrawer.BOTTOM, top: Int = AlphaDrawer.TOP): List<Int> {
		return (bottom..top).filter {
			it.mod12 in extension.map { (root + it).mod12 }
		}
	}

	/**
	 * Retrieves the closest tone in this chord to the given tone.
	 * If two tones in the chord are equally close, returns the lower one.
	 */
	fun closestTone(tone: Int): Int {
		for(i in 0..11) {
			when {
				containsTone(tone + i) -> return tone + i
				containsTone(tone  - i) -> return tone - i
			}
		}
		return root
	}

	val name: String
		get() = mod12Names[root] + heptatonics.colorString

	fun containsTone(tone: Int): Boolean = containsColor(tone - root)

	fun containsColor(color: Int): Boolean = extension.contains(color.mod12)

	val isMinor: Boolean get() = heptatonics.isMinor
	val isMajor: Boolean get() = heptatonics.isMajor
	val isAugmented: Boolean get() = isMajor && heptatonics.fifth == AUGMENTED
	val isDiminished: Boolean get() = isMinor && heptatonics.fifth == DIMINISHED
	val isSus: Boolean get() = heptatonics.isSus
	val hasMajor7: Boolean get() = heptatonics.hasMajor7
	val hasMinor7: Boolean get() = heptatonics.hasMinor7
	val hasMajor6: Boolean get() = heptatonics.hasMajor6
	val hasMinor6: Boolean get() = heptatonics.hasMinor6
	val hasAugmented5: Boolean get() = heptatonics.hasAugmented5
	val hasDiminished5: Boolean get() = heptatonics.hasDiminished5
	val hasAugmented4: Boolean get() = heptatonics.hasAugmented4
	val hasDiminished4: Boolean get() = heptatonics.hasDiminished4
	val hasMajor3: Boolean get() = heptatonics.hasMajor3
	val hasMinor3: Boolean get() = heptatonics.hasMinor3
	val hasAugmented2: Boolean get() = heptatonics.hasAugmented2
	val hasMajor2: Boolean get() = heptatonics.hasMajor2
	val hasMinor2: Boolean get() = heptatonics.hasMinor2
	val isDominant: Boolean get() = heptatonics.isDominant

	override fun toString() = "$name ($root+$extension)"

	companion object {
		val mod12Names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
		val mod7Names = arrayOf("C", "D", "E", "F", "G", "A", "B")

		@JvmField
		val CREATOR: Parcelable.Creator<Chord> = object : Parcelable.Creator<Chord> {
			override fun createFromParcel(parcel: Parcel): Chord {
				return Chord(parcel)
			}

			override fun newArray(size: Int): Array<Chord?> {
				return arrayOfNulls(size)
			}
		}
	}

	override fun describeContents(): Int = 0

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeInt(root)
		dest.writeInt(extension.size)
		dest.writeIntArray(extension)
	}
}
