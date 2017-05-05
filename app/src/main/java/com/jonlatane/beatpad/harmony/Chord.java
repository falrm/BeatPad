package com.jonlatane.beatpad.harmony;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by jonlatane on 5/5/17.
 */

public class Chord {
    static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final Integer[] MAJOR = {4, 7};
    public static final Integer[] MAJOR_6 = {4, 7, 9};
    public static final Integer[] MAJOR_6_9 = {2, 4, 7, 9};
    public static final Integer[] MAJOR_7 = {4, 7, 11};
    public static final Integer[] MINOR = {3, 7};
    public static final Integer[] MINOR_7 = {3, 7, 10};
    public static final Integer[] MINOR__MAJOR_7 = {3, 7, 11};
    public static final Integer[] DOM_7 = {4, 7, 10};

    final int octave;
    final int span;
    final int root;
    final List<Integer> extension = new LinkedList<>();
    final Set<Integer> colors = new HashSet<>();

    public Chord(int root, Integer[] extension, int octave, int span) {
        this.root = (1200 + root) % 12;
        this.octave = octave;
        this.span = span;
        addExtension(extension);
    }

    public Chord(int root, List<Integer> extension, int octave, int span) {
        this(root, extension.toArray(new Integer[extension.size()]), octave, span);
    }

    public Chord(int root, Integer[] extension, Chord c) {
        this(root, extension, c.octave, c.span);
    }

    public Chord(int root, List<Integer> extension, Chord c) {
        this(root, extension, c.octave, c.span);
    }

    public void addExtension(Integer[] extension) {
        for(Integer i : extension) {
            addTone(i);
        }
        Collections.sort(this.extension);
    }

    public List<Integer> getTones() {
        Integer normalRoot = root + (12 * (octave - 4));
        List<Integer> tones = new LinkedList<>(Collections.singleton(normalRoot));
        int relativeOctave = 0;
        Outer: while(true) {
            for(Integer color : extension) {
                Integer tone = normalRoot + color + 12*relativeOctave;
                if(tone - normalRoot <= span) {
                    tones.add(tone);
                } else {
                    break Outer;
                }
            }
            relativeOctave++;
        }
        return tones;
    }

    public String getName() {
        return NAMES[root]
                + (isDominant() ? "7" : isMinor() ? "m" : "");
    }

    public boolean isMinor() {
        return colors.contains(3);
    }

    public boolean isMajor() {
        return colors.contains(4) || !colors.contains(3);
    }

    public boolean isDominant() {
        return isMajor() && colors.contains(10);
    }

    private void addTone(Integer tone) {
        this.extension.add((1200 + tone) % 12);
        this.colors.add((1200 + tone) % 12);
    }
}
