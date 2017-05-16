package com.jonlatane.beatpad.harmony;

/**
 * Created by jonlatane on 5/5/17.
 */
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.harmony.chord.Heptatonics;

import static com.jonlatane.beatpad.harmony.chord.Chord.AUG;
import static com.jonlatane.beatpad.harmony.chord.Chord.DIM;
import static com.jonlatane.beatpad.harmony.chord.Chord.DOM_7;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ_7;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJ_ADD_9;
import static com.jonlatane.beatpad.harmony.chord.Chord.MIN;
import static com.jonlatane.beatpad.harmony.chord.Chord.MIN_7;
import static com.jonlatane.beatpad.harmony.chord.Chord.SUS;
import static com.jonlatane.beatpad.harmony.chord.Heptatonics.NONEXISTENT;

public abstract class ChordSequence {
    // Based on "Something Just Like This"
    public static ChordSequence CHAINSMOKERS = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            // D -> G(9)
            if(c.isMajor() && c.heptatonics.second() == NONEXISTENT) {
                return new Chord(c.root - 7, MAJ_ADD_9);
            }
            // Bm -> D
            if(c.isMinor() && !c.hasMinor7()) {
                return new Chord(c.root + 3, MAJ);
            }
            // Asus -> Bm
            if(c.isSus()) {
                return new Chord(c.root + 2, MIN);
            }
            // G(9) -> Asus
            return new Chord(c.root + 2, SUS);
        }

        @Override
        public Chord back(Chord c) {
            // D -> Bm
            if(c.isMajor() && c.heptatonics.second() == NONEXISTENT) {
                return new Chord(c.root - 3, MIN);
            }
            // Bm -> Asus
            if(c.isMinor() && !c.hasMinor7()) {
                return new Chord(c.root - 2, SUS);
            }
            // Asus -> G(9)
            if(c.isSus()) {
                return new Chord(c.root - 2, MAJ_ADD_9);
            }
            // G(9) -> D
            return new Chord(c.root + 7, MAJ);
        }
    };
    public static ChordSequence WHOLE_STEPS = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 2, c.extension);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 2, c.extension);
        }
    };
    public static ChordSequence NINES = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            switch(c.heptatonics.second()) {
                case NONEXISTENT:
                    return c.plus(2);
                case Heptatonics.MAJOR:
                    return c.replaceOrAdd(2, 3);
                default: return c.replaceOrAdd(1, 2);
            }
        }

        @Override
        public Chord back(Chord c) {
            switch(c.heptatonics.second()) {
                case Heptatonics.AUGMENTED:
                    return c.replaceOrAdd(3, 2);
                case Heptatonics.MAJOR:
                    return c.replaceOrAdd(2, 1);
                default: return c.replaceOrAdd(2, 1);
            }
        }
    };
    public static ChordSequence REL_MINOR_MAJOR = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMinor()) {
                return new Chord(c.root + 3, MAJ_7);
            }
            return new Chord(c.root + 4, MIN_7);
        }

        @Override
        public Chord back(Chord c) {
            if(c.isMinor()) {
                return new Chord(c.root - 4, MAJ_7);
            }
            return new Chord(c.root - 3, MIN_7);
        }
    };

    public static ChordSequence AUG_DIM = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMinor() && c.heptatonics.fifth() == Heptatonics.DIMINISHED) {
                return new Chord(c.root, MIN_7);
            }
            if(c.isMinor()) {
                return new Chord(c.root, DOM_7);
            }
            if(c.isDominant()) {
                return new Chord(c.root, MAJ_7);
            }
            return new Chord(c.root, AUG);
        }

        @Override
        public Chord back(Chord c) {
            if(c.isMajor() && c.heptatonics.fifth() == Heptatonics.AUGMENTED) {
                return new Chord(c.root, MAJ_7);
            }
            if(c.isMinor()) {
                return new Chord(c.root, DIM);
            }
            if(c.isMajor() && !c.isDominant()) {
                return new Chord(c.root, DOM_7);
            }
            return new Chord(c.root, MIN_7);
        }
    };
    public static ChordSequence CHROMATIC = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 1, c.extension);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 1, c.extension);
        }
    };
    public static ChordSequence CIRCLE_OF_FIFTHS = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root - 7, c.extension);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root + 7, c.extension);
        }
    };
    public static ChordSequence CIRCLE_OF_FOURTHS = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 7, c.extension);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 7, c.extension);
        }
    };
    public static ChordSequence TWO_FIVE_ONE = new ChordSequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMinor()) {
                return new Chord(c.root - 7, Chord.DOM_7);
            }
            if(c.isDominant()) {
                return new Chord(c.root - 7, Chord.MAJ_7);
            }
            return new Chord(c.root + 2, Chord.MIN_7);
        }

        @Override
        public Chord back(Chord c) {
            if(c.isDominant()) {
                return new Chord(c.root + 7, Chord.MIN_7);
            }
            if(c.isMinor()) {
                return new Chord(c.root - 2, Chord.MAJ_7);
            }
            return new Chord(c.root + 7, Chord.DOM_7);
        }
    };
    public abstract Chord forward(Chord c);
    public abstract Chord back(Chord c);
}
