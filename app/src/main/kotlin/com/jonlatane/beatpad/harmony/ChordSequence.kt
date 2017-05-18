package com.jonlatane.beatpad.harmony

/**
 * Created by jonlatane on 5/5/17.
 */
import com.jonlatane.beatpad.harmony.chord.*

interface ChordSequence {
    fun forward(c: Chord): Chord
    fun back(c: Chord): Chord
}
// Based on "Something Just Like This"
var CHAINSMOKERS: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        // D -> G(9)
        if (c.isMajor && c.heptatonics.second == NONEXISTENT) {
            return Chord(c.root - 7, MAJ_ADD_9)
        }
        // Bm -> D
        if (c.isMinor && !c.hasMinor7) {
            return Chord(c.root + 3, MAJ)
        }
        // Asus -> Bm
        if (c.isSus) {
            return Chord(c.root + 2, MIN)
        }
        // G(9) -> Asus
        return Chord(c.root + 2, SUS)
    }

    override fun back(c: Chord): Chord {
        // D -> Bm
        if (c.isMajor && c.heptatonics.second == NONEXISTENT) {
            return Chord(c.root - 3, MIN)
        }
        // Bm -> Asus
        if (c.isMinor && !c.hasMinor7) {
            return Chord(c.root - 2, SUS)
        }
        // Asus -> G(9)
        if (c.isSus) {
            return Chord(c.root - 2, MAJ_ADD_9)
        }
        // G(9) -> D
        return Chord(c.root + 7, MAJ)
    }
}
var WHOLE_STEPS: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 2, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 2, c.extension)
    }
}
var NINES: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        when (c.heptatonics.second) {
            NONEXISTENT -> return c.plus(2)
            MAJOR -> return c.replaceOrAdd(2, 3)
            else -> return c.replaceOrAdd(1, 2)
        }
    }

    override fun back(c: Chord): Chord {
        when (c.heptatonics.second) {
            AUGMENTED -> return c.replaceOrAdd(3, 2)
            MAJOR -> return c.replaceOrAdd(2, 1)
            else -> return c.replaceOrAdd(2, 1)
        }
    }
}
var REL_MINOR_MAJOR: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root + 3, MAJ_7)
        }
        return Chord(c.root + 4, MIN_7)
    }

    override fun back(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root - 4, MAJ_7)
        }
        return Chord(c.root - 3, MIN_7)
    }
}

var AUG_DIM: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        if (c.isMinor && c.heptatonics.fifth == DIMINISHED) {
            return Chord(c.root, MIN_7)
        }
        if (c.isMinor) {
            return Chord(c.root, DOM_7)
        }
        if (c.isDominant) {
            return Chord(c.root, MAJ_7)
        }
        return Chord(c.root, AUG)
    }

    override fun back(c: Chord): Chord {
        if (c.isMajor && c.heptatonics.fifth == AUGMENTED) {
            return Chord(c.root, MAJ_7)
        }
        if (c.isMinor) {
            return Chord(c.root, DIM)
        }
        if (c.isMajor && !c.isDominant) {
            return Chord(c.root, DOM_7)
        }
        return Chord(c.root, MIN_7)
    }
}
var CHROMATIC: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 1, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 1, c.extension)
    }
}
var CIRCLE_OF_FIFTHS: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root - 7, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root + 7, c.extension)
    }
}
var CIRCLE_OF_FOURTHS: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 7, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 7, c.extension)
    }
}
var TWO_FIVE_ONE: ChordSequence = object : ChordSequence {
    override fun forward(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root - 7, DOM_7)
        }
        if (c.isDominant) {
            return Chord(c.root - 7, MAJ_7)
        }
        return Chord(c.root + 2, MIN_7)
    }

    override fun back(c: Chord): Chord {
        if (c.isDominant) {
            return Chord(c.root + 7, MIN_7)
        }
        if (c.isMinor) {
            return Chord(c.root - 2, MAJ_7)
        }
        return Chord(c.root + 7, DOM_7)
    }
}
