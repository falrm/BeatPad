package com.jonlatane.beatpad.model.chord.heptatonics

import com.jonlatane.beatpad.model.chord.*

/**
 * A structure representing the Heptatonic (7-note, i.e. root plus 2 through 7 or
 * the 1, 3, 5, 7, 9, 11, 13) properties of a Chord.

 * The methods [.second], [.third], [.fourth], [.fifth],
 * [.sixth] and [.seventh] all return one of [.NONEXISTENT], [.MAJOR],
 * [.MINOR], [.PERFECT], [.AUGMENTED], or [.DIMINISHED].

 * The method getColorString()
 */
class Heptatonics(private val colors: Set<Int>) {
	val isMinor get() = !isMajor && colors.contains(3)
	val isDominant get() = isMajor && hasMinor7
	val isMajor get() = colors.contains(4)

	val isSus get() = third == NONEXISTENT

	val hasMajor7 get() = seventh == MAJOR
	val hasMinor7 get() = seventh == MINOR
	val has7 get() = seventh != NONEXISTENT
  val hasAugmented6 get() = false
	val hasMajor6 get() = sixth == MAJOR
	val hasMinor6 get() = sixth == MINOR
	val has6 get() = sixth != NONEXISTENT
	val hasAugmented5 get() = fifth == AUGMENTED
	val hasPerfect5 get() = fifth == PERFECT
	val hasDiminished5 get() = fifth == DIMINISHED
	val has5 get() = fifth != NONEXISTENT
	val hasAugmented4 get() = fourth == AUGMENTED
	val hasPerfect4 get() = fourth == PERFECT
	val hasDiminished4 get() = fourth == DIMINISHED
	val has4 get() = fifth != NONEXISTENT
	val hasMajor3 get() = third == MAJOR
	val hasMinor3 get() = third == MINOR
	val has3 get() = third != NONEXISTENT
	val hasAugmented2 get() = second == AUGMENTED
	val hasMajor2 get() = second == MAJOR
	val hasMinor2 get() = second == MINOR
	val has2 get() = second != NONEXISTENT


	/**  @return [.MINOR], [.MAJOR], or [.NONEXISTENT] */
	val third: Int = when {
		colors.contains(4) -> MAJOR
		colors.contains(3) -> MINOR
		else -> NONEXISTENT
	}

	/**  @return [.PERFECT], [.AUGMENTED], [.DIMINISHED] or [.NONEXISTENT] */
	val fifth: Int = when {
		colors.contains(7) -> PERFECT
		colors.contains(6) -> DIMINISHED
		colors.contains(8) -> AUGMENTED
		else -> NONEXISTENT
	}

	/**  @return [.MINOR], [.MAJOR] or [.NONEXISTENT] */
	val seventh: Int = when {
		colors.contains(11) -> MAJOR
		colors.contains(10) -> MINOR
		else -> NONEXISTENT
	}

	/**  @return [.MINOR], [.MAJOR], [.AUGMENTED] or [.NONEXISTENT] */
	val second: Int = when {
		colors.contains(1) -> MINOR
		colors.contains(2) -> MAJOR
		colors.contains(3) && third == MAJOR -> AUGMENTED
		else -> NONEXISTENT
	}

	/**  @return [.PERFECT], [.AUGMENTED], [.DIMINISHED] or [.NONEXISTENT] */
	val fourth: Int = when {
		colors.contains(5) -> PERFECT
		colors.contains(6) && fifth != DIMINISHED -> AUGMENTED
		else -> NONEXISTENT
	}

	/**  @return [.MINOR], [.MAJOR] or [.NONEXISTENT] */
	val sixth: Int = when {
		colors.contains(9) -> MAJOR
		colors.contains(8) && fifth != AUGMENTED -> MINOR
		else -> NONEXISTENT
	}

	val colorString: String by lazy {
    when {
      colors.size == 12 -> " Chrom."
      colors.size >= 10 -> " Chrom. (no ${(0..11).filter { !colors.contains(it) }
        .joinToString(",") {
          when (it) {
            1    -> "b2"
            2    -> "2"
            3    -> "b3"
            4    -> "3"
            5    -> "4"
            6    -> "b5"
            7    -> "5"
            8    -> "b6"
            9    -> "6"
            10   -> "b7"
            11   -> "M7"
            else -> TODO()
          }
        }})"
      else              -> {
        var colorString = ""
        // Artifically pad for readability.  degreeColors[2] is the second scale degree of the chord.
        val degreeColors = intArrayOf(0, 0, second, third, fourth, fifth, sixth, seventh)
        val namedColors = booleanArrayOf(false, false, false, false, false, false, false, false)
        // Check for 13 chords first
        if (degreeColors[6] == MAJOR && degreeColors[7] != NONEXISTENT) {
          if (degreeColors[7] != MINOR) {
            colorString += "M"
          }
          colorString += "13"
          namedColors[6] = true
          namedColors[7] = namedColors[6]
          if (degreeColors[2] == MAJOR) {
            namedColors[2] = true
          }
          // Cm13 is understood to have an 11, but not CM13.
          if (degreeColors[3] == MINOR) {
            namedColors[4] = true
          }
          // Then 11 chords
        } else if (degreeColors[4] == PERFECT && degreeColors[7] != NONEXISTENT) {
          if (degreeColors[7] != MINOR) {
            colorString += "M"
          }
          colorString += "11"
          namedColors[4] = true
          namedColors[7] = namedColors[4]
          if (degreeColors[2] == MAJOR) {
            namedColors[2] = true
          }
          // Then 9 chords
        } else if (degreeColors[2] == MAJOR && degreeColors[7] != NONEXISTENT) {
          if (degreeColors[7] != MINOR) {
            colorString += "M"
          }
          colorString += "9"
          namedColors[2] = true
          namedColors[7] = namedColors[2]
          // Finally 7 chords
        } else if (degreeColors[7] == MAJOR) {
          colorString += "M7"
          namedColors[7] = true
        } else if (degreeColors[7] == MINOR) {
          colorString += "7"
          namedColors[7] = true
          // And 6 chords
        } else if (degreeColors[6] == MAJOR) {
          colorString += "6"
          namedColors[6] = true
        }
        // Name the fifth
        if (degreeColors[5] == DIMINISHED) {
          colorString += "(b5)"
        } else if (degreeColors[5] == AUGMENTED) {
          colorString += "(#5)"
        }
        // Name the fourth
        namedColors[5] = true
        if (degreeColors[4] == DIMINISHED) {
          colorString += "(b11)"
        } else if (degreeColors[4] == AUGMENTED) {
          colorString += "(#11)"
        } else if (degreeColors[4] == PERFECT && !namedColors[4] && degreeColors[3] != NONEXISTENT) {
          colorString += "(11)"
        }
        namedColors[4] = true
        // Name the sixth/thirteenth
        if (degreeColors[6] == AUGMENTED) {
          colorString += "(#13)"
        } else if (degreeColors[6] == MINOR) {
          colorString += "(b13)"
        } else if (degreeColors[6] == MAJOR && !namedColors[6]) {
          colorString += "(6)"
        }
        // Name the ninth
        if (degreeColors[2] == AUGMENTED) {
          colorString += "(#9)"
        } else if (degreeColors[2] == MINOR) {
          colorString += "(b9)"
        } else if (degreeColors[2] == MAJOR && !namedColors[2]) {
          colorString += "(9)"
        }
        // Name minor chords
        if (degreeColors[3] == MINOR) {
          colorString = "m" + colorString
        }
        // Name sus chords
        if (degreeColors[3] == NONEXISTENT) {
          if (degreeColors[4] == PERFECT) {
            colorString = "sus" + colorString
          } else if (degreeColors[2] == MAJOR) {
            colorString = "sus2" + colorString
          } else {
            colorString = "5" + colorString
          }
        }
        colorString
      }
    }
	}
}
