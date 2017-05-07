package com.jonlatane.beatpad.harmony;

/**
 * Created by jonlatane on 5/5/17.
 */
import com.jonlatane.beatpad.harmony.chord.Chord;
import com.jonlatane.beatpad.harmony.chord.Heptatonics;

import static com.jonlatane.beatpad.harmony.chord.Chord.AUG;
import static com.jonlatane.beatpad.harmony.chord.Chord.DIM;
import static com.jonlatane.beatpad.harmony.chord.Chord.MAJOR_6;
import static com.jonlatane.beatpad.harmony.chord.Chord.MINOR_7;
import static com.jonlatane.beatpad.harmony.chord.Heptatonics.NONEXISTENT;

public abstract class Sequence {
    public static Sequence NINES = new Sequence() {
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
    public static Sequence REL_MINOR_MAJOR = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 3, MAJOR_6);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 3, MINOR_7);
        }
    };

    public static Sequence AUG_DIM = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMinor() && c.heptatonics.fifth() == Heptatonics.DIMINISHED) {
                return new Chord(c.root, MINOR_7);
            }
            if(c.isMinor()) {
                return new Chord(c.root, MAJOR_6);
            }
            return new Chord(c.root, AUG);
        }

        @Override
        public Chord back(Chord c) {
            if(c.isMajor() && c.heptatonics.fifth() == Heptatonics.AUGMENTED) {
                return new Chord(c.root, MAJOR_6);
            }
            if(c.isMinor()) {
                return new Chord(c.root, DIM);
            }
            return new Chord(c.root, MINOR_7);
        }
    };
    public static Sequence CHROMATIC = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 1, c.extension);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 1, c.extension);
        }
    };
    public static Sequence CIRCLE_OF_FIFTHS = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 7, c.extension);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 7, c.extension);
        }
    };
    public static Sequence TWO_FIVE_ONE = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMinor()) {
                return new Chord(c.root - 7, Chord.DOM_7);
            }
            if(c.isDominant()) {
                return new Chord(c.root - 7, Chord.MAJOR_6);
            }
            return new Chord(c.root + 2, Chord.MINOR_7);
        }

        @Override
        public Chord back(Chord c) {
            if(c.isDominant()) {
                return new Chord(c.root + 7, Chord.MINOR_7);
            }
            if(c.isMinor()) {
                return new Chord(c.root - 2, Chord.MAJOR_6);
            }
            return new Chord(c.root + 7, Chord.DOM_7);
        }
    };
    public abstract Chord forward(Chord c);
    public abstract Chord back(Chord c);
}
