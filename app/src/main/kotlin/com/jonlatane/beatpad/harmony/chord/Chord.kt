package com.jonlatane.beatpad.harmony.chord

import android.os.Parcel
import android.os.Parcelable
import com.jonlatane.beatpad.harmony.chord.heptatonics.Heptatonics
import com.jonlatane.beatpad.util.mod12

/**
 * Created by jonlatane on 5/5/17.
 */
class Chord : Parcelable {
	val root: Int
	val extension: IntArray
	val heptatonics: Heptatonics

	constructor(root: Int, extension: IntArray) {
		this.root = root.mod12
		this.extension = extension.map { it.mod12 }.toIntArray()
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
	 * For instance, replaceOrAdd(2,1) should turn any chord except a #9 chord into that chord with a b9
	 * @param outTone
	 * *
	 * @param inTone
	 * *
	 * @return A new chord.  Its extension will have all of this chord's instances of outTone replaced
	 * *         with inTone.  If none were found, inTone will be added.
	 */
	fun replaceOrAdd(outTone: Int, inTone: Int): Chord {
		var found = false
		val newExtension = IntArray(extension.size)
		for (i in extension.indices) {
			val tone = extension[i]
			if (tone == outTone) {
				found = true
				newExtension[i] = inTone
			} else {
				newExtension[i] = tone
			}
		}
		var result = Chord(root, newExtension)
		if (!found) {
			result = result.plus(inTone)
		}
		return result
	}

	/**

	 * @param bottom lowest allowed note, inclusive
	 * *
	 * @param top highest allowed note, inclusive
	 * *
	 * @return
	 */
	fun getTones(bottom: Int, top: Int): List<Int> {
		val middleRoot = root.mod12
		return (-5..5).flatMap {
			val rootInOctave = middleRoot + 12 * it
			extension.map { rootInOctave + it }
		}.filter { it in bottom..top }
		/*val tones = mutableListOf<Int>()
		var currentRoot = root - 144
		while (currentRoot + 12 < bottom) {
			currentRoot += 12
		}
		while (currentRoot + 12 <= top) {
			extension
				.map { currentRoot + it }
				.filterTo(tones) { it in bottom..top }
			currentRoot += 12
		}
		return tones.filter { it in bottom..top }*/
	}

	val name: String
		get() = mod12Names[root] + heptatonics.colorString

	fun containsTone(tone: Int): Boolean {
		return containsColor(tone - root)
	}

	fun containsColor(color: Int): Boolean {
		return extension.contains(color.mod12)
	}

	val isMinor: Boolean get() = heptatonics.isMinor

	val isMajor: Boolean get() = heptatonics.isMajor

	val isSus: Boolean get() = heptatonics.isSus

	val hasMinor7: Boolean get() = heptatonics.hasMinor7

	val hasMajor7: Boolean get() = heptatonics.hasMajor7

	val isDominant: Boolean get() = heptatonics.isDominant

	val isAugmented: Boolean get() = isMajor && heptatonics.fifth == AUGMENTED

	val isDiminished: Boolean get() = isMinor && heptatonics.fifth == DIMINISHED

	companion object {
		private val mod12Names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
		private val mod7Names = arrayOf("C", "D", "E", "F", "G", "A", "B")

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

	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeInt(root)
		dest.writeInt(extension.size)
		dest.writeIntArray(extension)
	}
}
