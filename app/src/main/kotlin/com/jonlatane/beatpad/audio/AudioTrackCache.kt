package com.jonlatane.beatpad.audio

import android.media.AudioTrack
import android.os.Build
import android.util.Pair
import android.util.SparseArray

import java.util.LinkedList

/**
 * A cache for AudioTrack resources in Android.  Theoretically you should get 32 tracks to do what
 * you will with, minus tracks for what other applications do in the background.  Effectively,
 * 32-note polyphony across whatever you can use to make AudioTracks.

 * Created by jonlatane on 7/19/15.
 */
object AudioTrackCache {
    private val TAG = "ToneGenerator"

    /** Maps ([.hashCode] of instrument -> note Int value with C4 = 0 -> [AudioTrack]  */
    private val trackData = SparseArray<SparseArray<AudioTrack>>()
    private val recentlyUsedTracks = LinkedList<Pair<Int, Int>>()


    /**
     * Get a looping AudioTrack exactly one period long for the given note.  This may be generated,
     * or it may come from the cache. The track requested will become the MRU element.

     * @param n the fundamental frequency
     * *
     * @param generator a generator for the track
     * *
     * @return the requested AudioTrack
     */
    fun getAudioTrackForNote(n: Int, generator: AudioTrackGenerator): AudioTrack {
        // Find the hashcode of the overtone series
        val generatorHashCode = generator.hashCode()

        // cacheLocation.first tells us the instrument/overtone series (via the hash of the Double[])
        // cacheLocation.second tells us the specific note
        val cacheLocation = Pair<Int, Int>(generatorHashCode, n)

        // Update list of recently used notes with this first
        recentlyUsedTracks.remove(cacheLocation)
        recentlyUsedTracks.addFirst(cacheLocation)

        // See if this note is in our cache
        var instrumentNotes = trackData.get(cacheLocation.first)
        if (instrumentNotes == null) {
            instrumentNotes = SparseArray()
            trackData.put(cacheLocation.first, instrumentNotes)
        }
        var result = instrumentNotes.get(n)

        // If not, generate it
        if (result == null) {
            result = generator.getAudioTrackFor(n)
        }

        instrumentNotes.put(n, result)

        return result
    }

    /**
     * Release all AudioTracks created by this cache

     * @return false unless there was an error
     */
    fun releaseAll(): Boolean {
        val result = releaseOne()
        var shouldLoopAgain = result
        while (shouldLoopAgain == true) {
            shouldLoopAgain = releaseOne()
        }
        return result
    }

    /**
     * Release all AudioTracks using the given overtone series

     * @param generator a generator with a unique hashCode
     * *
     * @return true on success
     */
    fun releaseAll(generator: AudioTrackGenerator): Boolean {
        val instrumentNotes = trackData.get(generator.hashCode()) ?: return true
        var result = true
        for (i in 0..instrumentNotes.size() - 1) {
            try {
                val goodbyeCruelWorld = instrumentNotes.valueAt(i)
                goodbyeCruelWorld.stop()
                goodbyeCruelWorld.flush()
                goodbyeCruelWorld.release()
            } catch (e: Throwable) {
                result = false
            }

        }
        instrumentNotes.clear()
        return result
    }

    /**
     * Release the least recently used AudioTrack resource to free up resources

     * @return true if a track was successfully removed
     */
    fun releaseOne(): Boolean {
        var result = true
        try {
            val lruNote = recentlyUsedTracks.removeLast()
            val goodbyeCruelWorld = trackData.get(lruNote.first).get(lruNote.second)
            goodbyeCruelWorld.flush()
            goodbyeCruelWorld.release()
            trackData.get(lruNote.first).remove(lruNote.second)
        } catch (e: Throwable) {
            result = false
        }

        return result
    }

    fun normalizeVolumes() {
        val max = AudioTrack.getMaxVolume()
        val min = AudioTrack.getMinVolume()
        val span = max - min

        val currentlyPlaying = SparseArray<MutableList<AudioTrack>>()
        for (i in 0..trackData.size() - 1) {
            val noteTracks = trackData.valueAt(i)
            for (j in 0..noteTracks.size() - 1) {
                val t = noteTracks.valueAt(j)
                if (t.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    val n = trackData.keyAt(i)
                    var l = currentlyPlaying.get(n)
                    if (l == null) {
                        l = mutableListOf()
                        currentlyPlaying.put(n, l)
                    }
                    l.add(t)
                }
            }
        }

        for (i in 0..currentlyPlaying.size() - 1) {
            val n = currentlyPlaying.keyAt(i)
            val l = currentlyPlaying.valueAt(i)
            for (t in l) {
                // Lower notes are amped up so turn them down a bit with this factor
                // This number between 0 and 1 by which we will increase volume
                val totalNumNotesRedFactor = (1 / currentlyPlaying.size()).toFloat()
                val adjusted = min + span * (.05 * Math.atan((n + 5).toFloat() / 88.0) + .9) * totalNumNotesRedFactor
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    t.setVolume(adjusted.toFloat())
                } else {
                    t.setStereoVolume(adjusted.toFloat(), adjusted.toFloat())
                }
            }
        }
    }
}
