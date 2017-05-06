package com.jonlatane.beatpad.harmony;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.primitives.Ints;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by jonlatane on 5/5/17.
 */

public class Chord implements Parcelable {
    static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final Integer[] MAJOR = {4, 7};
    public static final Integer[] MAJOR_6 = {4, 7, 9};
    public static final Integer[] MAJOR_6_9 = {2, 4, 7, 9};
    public static final Integer[] MAJOR_7 = {4, 7, 11};
    public static final Integer[] MINOR = {3, 7};
    public static final Integer[] MINOR_7 = {3, 7, 10};
    public static final Integer[] MINOR__MAJOR_7 = {3, 7, 11};
    public static final Integer[] DOM_7 = {4, 7, 10};
    public static final Integer[] DIM = {3, 6};
    public static final Integer[] AUG = {4, 8};

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
        List<Integer> tones = new LinkedList<>();
        int relativeOctave = 0;
        Outer: while(true) {
            tones.add(normalRoot + 12 * relativeOctave);
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
                + (isDominant() ? "7" : isMinor() ? "m" : "")
                + (isDiminished() ? "b5" : isAugmented() ? "#5" : "");
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

    public boolean isDiminished() {
        return colors.contains(6) && !hasPerfectFifth();
    }

    public boolean isAugmented() {
        return colors.contains(8) && !hasPerfectFifth();
    }

    private boolean hasPerfectFifth() {
        return colors.contains(7);
    }

    private void addTone(Integer tone) {
        this.extension.add((1200 + tone) % 12);
        this.colors.add((1200 + tone) % 12);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(octave);
        dest.writeInt(span);
        dest.writeInt(root);
        dest.writeInt(extension.size());
        dest.writeIntArray(Ints.toArray(extension));
    }

    public static final Creator<Chord> CREATOR = new Creator<Chord>() {
        @Override
        public Chord createFromParcel(Parcel in) {
            return new Chord(in);
        }

        @Override
        public Chord[] newArray(int size) {
            return new Chord[size];
        }
    };

    private Chord(Parcel in) {
        octave = in.readInt();
        span = in.readInt();
        root = in.readInt();
        int[] extensionTones = new int[in.readInt()];
        in.readIntArray(extensionTones);
        for(int i : extensionTones) {
            addTone(i);
        }
    }
}
