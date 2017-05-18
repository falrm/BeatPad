package com.jonlatane.beatpad.instrument

import com.jonlatane.beatpad.sensors.Orientation

/**
 * Created by jonlatane on 5/5/17.
 */
class SequencerThread(instrument: Instrument, beatsPerMinute: Int) : DeviceOrientationInstrument(instrument), Runnable {

    @Volatile var beatsPerMinute: Int = 0
    @Volatile var stopped = false

    private val subDivisions = booleanArrayOf(true, true)


    init {
        this.beatsPerMinute = beatsPerMinute.toInt()
    }


    override fun run() {
        while (!stopped) {
            playBeat()
        }
    }

    private fun playBeat() {
        try {
            val msBetweenSubdivisions = 60000L / (beatsPerMinute * subDivisions.size)

            for (subDivision in subDivisions) {
                val relativeRoll = rollForArticulation
                val playDuration = (relativeRoll * msBetweenSubdivisions).toLong()
                val pauseDuration = msBetweenSubdivisions - playDuration

                // Interpret the booleans as "play" or "rest"
                if (subDivision) {
                    play()
                    Thread.sleep(playDuration)
                    stop()
                    Thread.sleep(pauseDuration)
                } else {
                    Thread.sleep(msBetweenSubdivisions)
                }
                if (stopped) {
                    break
                }
            }
        } catch (ignored: InterruptedException) {
        }

    }

    /**
     * Return device roll in the range [0.3f, 1.0f]
     * @return
     */
    private val rollForArticulation: Float
        get() {
            val result = Math.min(Math.max(0.3f, 3f * Orientation.normalizedDeviceRoll() * 0.7f + 0.65f), 1.0f)
            return result
        }

    companion object {
        private val TAG = SequencerThread::class.java!!.getSimpleName()
    }
}
