package com.jonlatane.beatpad.harmony

import com.jonlatane.beatpad.harmony.chord.*

// Based on "Something Just Like This"
object CHAINSMOKERS: ChordSequence {
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
object WHOLE_STEPS: ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 2, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 2, c.extension)
    }
}
object NINES: ChordSequence {
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
object MAJOR_MINOR_SECONDS: ChordSequence {
    override fun forward(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root + 3, MAJ_7)
        }
        return Chord(c.root + 2, MIN_7)
    }

    override fun back(c: Chord): Chord {
        if (c.isMinor) {
            return Chord(c.root - 2, MAJ_7)
        }
        return Chord(c.root - 3, MIN_7)
    }
}
object MAJOR_MINOR_THIRDS: ChordSequence {
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

object AUG_DIM: ChordSequence {
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
object CHROMATIC: ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 1, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 1, c.extension)
    }
}
object CIRCLE_OF_FIFTHS: ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root - 7, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root + 7, c.extension)
    }
}
object CIRCLE_OF_FOURTHS: ChordSequence {
    override fun forward(c: Chord): Chord {
        return Chord(c.root + 7, c.extension)
    }

    override fun back(c: Chord): Chord {
        return Chord(c.root - 7, c.extension)
    }
}
object TWO_FIVE_ONE: ChordSequence {
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
