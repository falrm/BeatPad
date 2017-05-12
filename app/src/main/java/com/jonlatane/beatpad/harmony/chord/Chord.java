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
    private static final String TAG = Chord.class.getSimpleName();
    static final String[] NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    public static final int[] MAJ = {0, 4, 7};
    public static final int[] MAJ_6 = {0, 4, 7, 9};
    public static final int[] MAJ_6_9 = {0, 2, 4, 7, 9};
    public static final int[] MAJ_7 = {0, 4, 7, 11};
    public static final int[] MIN = {0, 3, 7};
    public static final int[] MIN_7 = {0, 3, 7, 10};
    public static final int[] MIN_MAJ_7 = {0, 3, 7, 11};
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
     * Gets a derivative chord created by replacing all of the outTones with inTones.
     * For instance, replaceOrAdd(2,1) should turn any chord except a #9 chord into that chord with a b9
     * @param outTone
     * @param inTone
     * @return A new chord.  Its extension will have all of this chord's instances of outTone replaced
     *         with inTone.  If none were found, inTone will be added.
     */
    public Chord replaceOrAdd(int outTone, int inTone) {
        boolean found = false;
        int[] newExtension = new int[extension.length];
        for(int i = 0; i < extension.length; i++) {
            int tone = extension[i];
            if(tone == outTone) {
                found = true;
                newExtension[i] = inTone;
            } else {
                newExtension[i] = tone;
            }
        }
        Chord result = new Chord(root, newExtension);
        if(!found) {
            result = result.plus(inTone);
        }
        return result;
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
        while(currentRoot + 12 <= top) {
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

    public boolean containsTone(int tone) {
        return containsColor(tone - root);
    }

    public boolean containsColor(int color) {
        color = (1200 + color) % 12;
        for(int i : extension) {
            if(color == i) return true;
        }
        return false;
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
