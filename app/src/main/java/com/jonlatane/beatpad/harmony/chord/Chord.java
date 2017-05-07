package com.jonlatane.beatpad.harmony.chord;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jonlatane on 5/5/17.
 */

public class Chord implements Parcelable {
    static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final int[] MAJOR = {0, 4, 7};
    public static final int[] MAJOR_6 = {0, 4, 7, 9};
    public static final int[] MAJOR_6_9 = {0, 2, 4, 7, 9};
    public static final int[] MAJOR_7 = {0, 4, 7, 11};
    public static final int[] MINOR = {0, 3, 7};
    public static final int[] MINOR_7 = {0, 3, 7, 10};
    public static final int[] MINOR__MAJOR_7 = {0, 3, 7, 11};
    public static final int[] DOM_7 = {0, 4, 7, 10};
    public static final int[] DIM = {0, 3, 6};
    public static final int[] AUG = {0, 4, 8};

    public final int root;
    public final int[] extension;
    public final Heptatonics heptatonics;

    public Chord(int root, int[] extension) {
        this.root = (1200 + root) % 12;
        this.heptatonics = new Heptatonics(root);
        this.extension = extension;
        init();
    }

    private Chord(Parcel in) {
        root = in.readInt();
        heptatonics = new Heptatonics(root);
        extension = new int[in.readInt()];
        in.readIntArray(extension);
        init();
    }

    private void init() {
        for(int i = 0; i < extension.length; i++) {
            int tone = (1200 + extension[i]) % 12;
            extension[i] = tone;
            heptatonics.colors.add(tone);
        }
        Arrays.sort(extension);
    }

    public Chord plus(int... newTones) {
        int[] newExtension = new int[extension.length + newTones.length];
        System.arraycopy(extension, 0, newExtension, 0, extension.length);
        System.arraycopy(newTones, 0, newExtension, extension.length, newTones.length);
        return new Chord(root, newExtension);
    }

    /**
     *
     * @param bottom lowest allowed note, inclusive
     * @param top highest allowed note, inclusive
     * @return
     */
    public List<Integer> getTones(int bottom, int top) {
        List<Integer> tones = new LinkedList<>();
        int currentRoot = root - 144;
        while(currentRoot + 12 < bottom) {
            currentRoot += 12;
        }
        while(currentRoot + 24 < top) {
            for(int color : extension) {
                int tone = currentRoot + color;
                if(tone >= bottom && tone <= top) {
                    tones.add(tone);
                }
            }
            currentRoot += 12;
        }
        return tones;
    }

    public String getName() {
        return NAMES[root]+ heptatonics.getColorString();
    }

    public boolean isMinor() {
        return heptatonics.isMinor();
    }

    public boolean isMajor() {
        return heptatonics.isMajor();
    }

    public boolean isDominant() {
        return heptatonics.isDominant();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(root);
        dest.writeInt(extension.length);
        dest.writeIntArray(extension);
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
}
