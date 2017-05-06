package com.jonlatane.beatpad.harmony;

/**
 * Created by jonlatane on 5/5/17.
 */
import static com.jonlatane.beatpad.harmony.Chord.*;

public abstract class Sequence {


    public static Sequence AUG_DIM = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMajor()) {
                return new Chord(c.root, AUG, c);
            }
            if(c.isMinor()) {
                return new Chord(c.root, DIM, c);
            }
            return c;
        }

        @Override
        public Chord back(Chord c) {
            if(c.isMajor()) {
                return new Chord(c.root, MAJOR_6, c);
            }
            if(c.isMinor()) {
                return new Chord(c.root, MINOR_7, c);
            }
            return c;
        }
    };
    public static Sequence CHROMATIC = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 1, c.extension, c);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 1, c.extension, c);
        }
    };
    public static Sequence OCTAVES = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root, c.extension, Math.min(c.octave + 1, 7), c.span);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root, c.extension, Math.max(c.octave - 1, 0), c.span);
        }
    };
    public static Sequence CIRCLE_OF_FIFTHS = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            return new Chord(c.root + 7, c.extension, c);
        }

        @Override
        public Chord back(Chord c) {
            return new Chord(c.root - 7, c.extension, c);
        }
    };
    public static Sequence TWO_FIVE_ONE = new Sequence() {
        @Override
        public Chord forward(Chord c) {
            if(c.isMinor()) {
                return new Chord(c.root - 7, Chord.DOM_7, c);
            }
            if(c.isDominant()) {
                return new Chord(c.root - 7, Chord.MAJOR_6, c);
            }
            return new Chord(c.root + 2, Chord.MINOR_7, c);
        }

        @Override
        public Chord back(Chord c) {
            if(c.isDominant()) {
                return new Chord(c.root + 7, Chord.MINOR_7, c);
            }
            if(c.isMinor()) {
                return new Chord(c.root - 2, Chord.MAJOR_6, c);
            }
            return new Chord(c.root + 7, Chord.DOM_7, c);
        }
    };
    public abstract Chord forward(Chord c);
    public abstract Chord back(Chord c);
}
