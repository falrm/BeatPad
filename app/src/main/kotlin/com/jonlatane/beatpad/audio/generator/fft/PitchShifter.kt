package com.jonlatane.beatpad.audio.generator.fft

/**
 * Created by jonlatane on 7/18/15.
 */
object PitchShifter {

    private val MAX_FRAME_LENGTH = 16000
    private val gInFIFO = FloatArray(MAX_FRAME_LENGTH)
    private val gOutFIFO = FloatArray(MAX_FRAME_LENGTH)
    private val gFFTworksp = FloatArray(2 * MAX_FRAME_LENGTH)
    private val gLastPhase = FloatArray(MAX_FRAME_LENGTH / 2 + 1)
    private val gSumPhase = FloatArray(MAX_FRAME_LENGTH / 2 + 1)
    private val gOutputAccum = FloatArray(2 * MAX_FRAME_LENGTH)
    private val gAnaFreq = FloatArray(MAX_FRAME_LENGTH)
    private val gAnaMagn = FloatArray(MAX_FRAME_LENGTH)
    private val gSynFreq = FloatArray(MAX_FRAME_LENGTH)
    private val gSynMagn = FloatArray(MAX_FRAME_LENGTH)
    private var gRover: Long = 0
    private val gInit: Long = 0

    fun pitchShift(pitchShift: Float, numSampsToProcess: Long,
                   sampleRate: Float, indata: FloatArray): FloatArray {
        return pitchShift(pitchShift, numSampsToProcess, 2048.toLong(), 10.toLong(), sampleRate, indata)
    }

    fun toFloatArray(in_buff: ByteArray, in_offset: Int,
                     out_buff: FloatArray, out_offset: Int, out_len: Int): FloatArray {
        var ix = in_offset
        val len = out_offset + out_len
        for (ox in out_offset..len - 1) {
            out_buff[ox] = (in_buff[ix++].toInt() and 0xFF or (in_buff[ix++].toInt() shl 8)).toShort() * (1.0f / 32767.0f)
        }
        return out_buff
    }

    private fun pitchShift(pitchShift: Float, numSampsToProcess: Long, fftFrameSize: Long,
                           osamp: Long, sampleRate: Float, indata: FloatArray): FloatArray {
        var magn: Double
        var phase: Double
        var tmp: Double
        var window: Double
        var real: Double
        var imag: Double
        val freqPerBin: Double
        val expct: Double
        var i: Long
        var k: Long
        var qpd: Long
        var index: Long
        val inFifoLatency: Long
        val stepSize: Long
        val fftFrameSize2: Long


        val outdata = indata
        /* set up some handy variables */
        fftFrameSize2 = fftFrameSize / 2
        stepSize = fftFrameSize / osamp
        freqPerBin = sampleRate / fftFrameSize.toDouble()
        expct = 2.0 * Math.PI * stepSize.toDouble() / fftFrameSize.toDouble()
        inFifoLatency = fftFrameSize - stepSize
        if (gRover == 0L) gRover = inFifoLatency


        /* main processing loop */
        i = 0
        while (i < numSampsToProcess) {

            /* As long as we have not yet collected enough data just read in */
            gInFIFO[gRover.toInt()] = indata[i.toInt()]
            outdata[i.toInt()] = gOutFIFO[(gRover - inFifoLatency).toInt()]
            gRover++

            /* now we have enough data for processing */
            if (gRover >= fftFrameSize) {
                gRover = inFifoLatency

                /* do windowing and re,im interleave */
                k = 0
                while (k < fftFrameSize) {
                    window = -.5 * Math.cos(2.0 * Math.PI * k.toDouble() / fftFrameSize.toDouble()) + .5
                    gFFTworksp[(2 * k).toInt()] = (gInFIFO[k.toInt()] * window).toFloat()
                    gFFTworksp[(2 * k + 1).toInt()] = 0.0f
                    k++
                }


                /* ***************** ANALYSIS ******************* */
                /* do transform */
                shortTimeFourierTransform(gFFTworksp, fftFrameSize, -1)

                /* this is the analysis step */
                k = 0
                while (k <= fftFrameSize2) {

                    /* de-interlace FFT buffer */
                    real = gFFTworksp[(2 * k).toInt()].toDouble()
                    imag = gFFTworksp[(2 * k + 1).toInt()].toDouble()

                    /* compute magnitude and phase */
                    magn = 2.0 * Math.sqrt(real * real + imag * imag)
                    phase = Math.atan2(imag, real)

                    /* compute phase difference */
                    tmp = phase - gLastPhase[k.toInt()]
                    gLastPhase[k.toInt()] = phase.toFloat()

                    /* subtract expected phase difference */
                    tmp -= k.toDouble() * expct

                    /* map delta phase into +/- Pi interval */
                    qpd = (tmp / Math.PI).toLong()
                    if (qpd >= 0)
                        qpd += qpd and 1
                    else
                        qpd -= qpd and 1
                    tmp -= Math.PI * qpd.toDouble()

                    /* get deviation from bin frequency from the +/- Pi interval */
                    tmp = osamp * tmp / (2.0 * Math.PI)

                    /* compute the k-th partials' true frequency */
                    tmp = k.toDouble() * freqPerBin + tmp * freqPerBin

                    /* store magnitude and true frequency in analysis arrays */
                    gAnaMagn[k.toInt()] = magn.toFloat()
                    gAnaFreq[k.toInt()] = tmp.toFloat()
                    k++

                }

                /* ***************** PROCESSING ******************* */
                /* this does the actual pitch shifting */
                for (zero in 0..fftFrameSize - 1) {
                    gSynMagn[zero.toInt()] = 0f
                    gSynFreq[zero.toInt()] = 0f
                }

                k = 0
                while (k <= fftFrameSize2) {
                    index = (k * pitchShift).toLong()
                    if (index <= fftFrameSize2) {
                        gSynMagn[index.toInt()] += gAnaMagn[k.toInt()]
                        gSynFreq[index.toInt()] = gAnaFreq[k.toInt()] * pitchShift
                    }
                    k++
                }

                /* ***************** SYNTHESIS ******************* */
                /* this is the synthesis step */
                k = 0
                while (k <= fftFrameSize2) {

                    /* get magnitude and true frequency from synthesis arrays */
                    magn = gSynMagn[k.toInt()].toDouble()
                    tmp = gSynFreq[k.toInt()].toDouble()

                    /* subtract bin mid frequency */
                    tmp -= k.toDouble() * freqPerBin

                    /* get bin deviation from freq deviation */
                    tmp /= freqPerBin

                    /* take osamp into account */
                    tmp = 2.0 * Math.PI * tmp / osamp

                    /* add the overlap phase advance back in */
                    tmp += k.toDouble() * expct

                    /* accumulate delta phase to get bin phase */
                    gSumPhase[k.toInt()] += tmp.toFloat()
                    phase = gSumPhase[k.toInt()].toDouble()

                    /* get real and imag part and re-interleave */
                    gFFTworksp[(2 * k).toInt()] = (magn * Math.cos(phase)).toFloat()
                    gFFTworksp[(2 * k + 1).toInt()] = (magn * Math.sin(phase)).toFloat()
                    k++
                }

                /* zero negative frequencies */
                k = fftFrameSize + 2
                while (k < 2 * fftFrameSize) {
                    gFFTworksp[k.toInt()] = 0.0f
                    k++
                }

                /* do inverse transform */
                shortTimeFourierTransform(gFFTworksp, fftFrameSize, 1)

                /* do windowing and add to output accumulator */
                k = 0
                while (k < fftFrameSize) {
                    window = -.5 * Math.cos(2.0 * Math.PI * k.toDouble() / fftFrameSize.toDouble()) + .5
                    gOutputAccum[k.toInt()] += (2.0 * window * gFFTworksp[(2 * k).toInt()].toDouble() / (fftFrameSize2 * osamp)).toFloat()
                    k++
                }
                k = 0
                while (k < stepSize) {
                    gOutFIFO[k.toInt()] = gOutputAccum[k.toInt()]
                    k++
                }

                /* shift accumulator */
                //memmove(gOutputAccum, gOutputAccum + stepSize, fftFrameSize * sizeof(float));
                k = 0
                while (k < fftFrameSize) {
                    gOutputAccum[k.toInt()] = gOutputAccum[(k + stepSize).toInt()]
                    k++
                }

                /* move input FIFO */
                k = 0
                while (k < inFifoLatency) {
                    gInFIFO[k.toInt()] = gInFIFO[(k + stepSize).toInt()]
                    k++
                }
            }
            i++
        }
        return outdata
    }

    private fun shortTimeFourierTransform(fftBuffer: FloatArray, fftFrameSize: Long, sign: Long) {
        var wr: Float
        var wi: Float
        var arg: Float
        var temp: Float
        var tr: Float
        var ti: Float
        var ur: Float
        var ui: Float
        var i: Long
        var bitm: Long
        var j: Long
        var le: Long
        var le2: Long
        var k: Long

        i = 2
        while (i < 2 * fftFrameSize - 2) {
            bitm = 2
            j = 0
            while (bitm < 2 * fftFrameSize) {
                if (i and bitm != 0L) j++
                j = j shl 1
                bitm = bitm shl 1
            }
            if (i < j) {
                temp = fftBuffer[i.toInt()]
                fftBuffer[i.toInt()] = fftBuffer[j.toInt()]
                fftBuffer[j.toInt()] = temp
                temp = fftBuffer[(i + 1).toInt()]
                fftBuffer[(i + 1).toInt()] = fftBuffer[(j + 1).toInt()]
                fftBuffer[(j + 1).toInt()] = temp
            }
            i += 2
        }
        k = 0
        le = 2
        while (k < Math.log(fftFrameSize.toDouble()) / Math.log(2.0) + .5) {
            le = le shl 1
            le2 = le shr 1
            ur = 1.0f
            ui = 0.0f
            arg = Math.PI.toFloat() / (le2 shr 1)
            wr = Math.cos(arg.toDouble()).toFloat()
            wi = (sign * Math.sin(arg.toDouble())).toFloat()
            j = 0
            while (j < le2) {

                i = j
                while (i < 2 * fftFrameSize) {
                    tr = fftBuffer[(i + le2).toInt()] * ur - fftBuffer[(i + le2 + 1).toInt()] * ui
                    ti = fftBuffer[(i + le2).toInt()] * ui + fftBuffer[(i + le2 + 1).toInt()] * ur
                    fftBuffer[(i + le2).toInt()] = fftBuffer[i.toInt()] - tr
                    fftBuffer[(i + le2 + 1).toInt()] = fftBuffer[(i + 1).toInt()] - ti
                    fftBuffer[i.toInt()] += tr
                    fftBuffer[(i + 1).toInt()] += ti
                    i += le

                }
                tr = ur * wr - ui * wi
                ui = ur * wi + ui * wr
                ur = tr
                j += 2
            }
            k++
        }
    }
}
