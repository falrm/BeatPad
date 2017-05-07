package com.jonlatane.beatpad.harmony.chord;

import java.util.HashSet;
import java.util.Set;

/**
 * A structure representing the Heptatonic (7-note, i.e. root plus 2 through 7 or
 * the 1, 3, 5, 7, 9, 11, 13) properties of a Chord.
 *
 * The methods {@link #second()}, {@link #third()}, {@link #fourth()}, {@link #fifth()},
 * {@link #sixth()} and {@link #seventh()} all return one of {@link #NONEXISTENT}, {@link #MAJOR},
 * {@link #MINOR}, {@link #PERFECT}, {@link #AUGMENTED}, or {@link #DIMINISHED}.
 *
 * The method getColorString()
 *
 * Created by jonlatane on 5/6/17.
 */
public final class Heptatonics {
    final int root;
    final Set<Integer> colors;

    Heptatonics(int root) {
        this.root = root;
        this.colors = new HashSet<>();
    }


    public static final int DIMINISHED = 1;
    public static final int MINOR = 2;
    public static final int PERFECT = 3;
    public static final int MAJOR = 4;
    public static final int AUGMENTED = 5;
    public static final int NONEXISTENT = 0;

    public String getColorString() {
        String colorString = "";
        // Artifically pad for readability.  degreeColors[2] is the second scale degree of the chord.
        int[] degreeColors    = {0    , 0,     second(), third(), fourth(), fifth(), sixth(), seventh()};
        boolean[] namedColors = {false, false, false,    false,   false,    false,   false,   false    };

        // Check for 13 chords first
        if(degreeColors[6] == MAJOR
                && degreeColors[7] != NONEXISTENT) {
            if(degreeColors[7] != MINOR) {
                colorString += "M";
            }
            colorString += "13";
            namedColors[7] = namedColors[6] = true;
            if(degreeColors[2] == MAJOR) {
                namedColors[2] = true;
            }
            if(degreeColors[3] == MINOR) {
                namedColors[4] = true; // Cm13 is understood to have an 11, but not CM13.
            }
        }
        // Then 11 chords
        else if(degreeColors[4] == MAJOR
                && degreeColors[7] != NONEXISTENT) {
            if(degreeColors[7] != MINOR) {
                colorString += "M";
            }
            colorString += "11";
            namedColors[7] = namedColors[4] = true;
            if(degreeColors[2] == MAJOR) {
                namedColors[2] = true;
            }
        }
        // Then 9 chords
        else if(degreeColors[2] == MAJOR
                && degreeColors[7] != NONEXISTENT) {
            if(degreeColors[7] != MINOR) {
                colorString += "M";
            }
            colorString += "9";
            namedColors[7] = namedColors[2] = true;
        }
        // Finally 7 chords
        else if(degreeColors[7] == MAJOR) {
            colorString +="M7";
            namedColors[7] = true;
        } else if(degreeColors[7] == MINOR) {
            colorString +="7";
            namedColors[7] = true;
            // And 6 chords
        } else if(degreeColors[6] == MAJOR) {
            colorString += "6";
            namedColors[6] = true;
        }

        // Name the fifth
        if(degreeColors[5] == DIMINISHED) {
            colorString += "(b5)";
        } else if(degreeColors[5] == AUGMENTED){
            colorString += "(#5)";
        }
        namedColors[5] = true;

        // Name the fourth
        if(degreeColors[4] == DIMINISHED) {
            colorString += "(b11)";
        } else if(degreeColors[4] == AUGMENTED){
            colorString += "(#11)";
        } else if(degreeColors[4] == PERFECT && !namedColors[4]) {
            colorString += "(11)";
        }
        namedColors[4] = true;

        // Name the sixth/thirteenth
        if(degreeColors[6] == AUGMENTED) {
            colorString += "(#13)";
        } else if(degreeColors[6] == MINOR) {
            colorString += "(b13)";
        } else if(degreeColors[6] == MAJOR && !namedColors[6]) {
            colorString += "(6)";
        }

        // Name the ninth
        if(degreeColors[2] == AUGMENTED) {
            colorString += "(#9)";
        } else if(degreeColors[2] == MINOR) {
            colorString += "(b9)";
        } else if(degreeColors[2] == MAJOR && !namedColors[2]) {
            colorString += "(9)";
        }

        // Name minor chords
        if(degreeColors[3] == MINOR) {
            colorString = "m" + colorString;
        }

        return colorString;
    }

    public boolean isMinor() {
        return colors.contains(3);
    }

    public boolean isMajor() {
        return colors.contains(4) || !isMinor();
    }

    public boolean isDominant() {
        return isMajor() && colors.contains(10);
    }

    /**  @return {@link #MINOR}, {@link #MAJOR} or {@link #NONEXISTENT} */
    public int seventh() {
        return colors.contains(11) ? MAJOR
                : colors.contains(10) ? MINOR
                : NONEXISTENT;
    }
    /**  @return {@link #MINOR}, {@link #MAJOR} or {@link #NONEXISTENT} */
    public int sixth() {
        return colors.contains(9) ? MAJOR
                : colors.contains(8) && (fifth() != AUGMENTED) ? MINOR
                : NONEXISTENT;
    }
    /**  @return {@link #PERFECT}, {@link #AUGMENTED}, {@link #DIMINISHED} or {@link #NONEXISTENT} */
    public int fifth() {
        return colors.contains(7) ? PERFECT
                : colors.contains(6) ? DIMINISHED
                : colors.contains(8) ? AUGMENTED
                : NONEXISTENT;
    }
    /**  @return {@link #PERFECT}, {@link #AUGMENTED}, {@link #DIMINISHED} or {@link #NONEXISTENT} */
    public int fourth() {
        return colors.contains(5) ? PERFECT
                : colors.contains(6) && fifth() != DIMINISHED ? AUGMENTED
                : colors.contains(8) && sixth() == MAJOR ? AUGMENTED
                : NONEXISTENT;
    }
    /**  @return {@link #MINOR}, {@link #MAJOR}, or {@link #NONEXISTENT} */
    public int third() {
        return colors.contains(4) ? MAJOR
                : colors.contains(3) ? MINOR
                : NONEXISTENT;
    }
    /**  @return {@link #MINOR}, {@link #MAJOR}, {@link #AUGMENTED} or {@link #NONEXISTENT} */
    public int second() {
        return colors.contains(1) ? MINOR
                : colors.contains(2) ? MAJOR
                : colors.contains(3) && third() == MAJOR ? AUGMENTED
                : NONEXISTENT;
    }
}
