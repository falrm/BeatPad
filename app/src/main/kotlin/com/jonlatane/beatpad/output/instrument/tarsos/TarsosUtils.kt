package com.jonlatane.beatpad.output.instrument.tarsos

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor

/**
 * Created by jonlatane on 5/29/17.
 */

//val dispatcher = AudioDispatcherFactory.
// Example pitch-detecting processor
private val processor = PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050f, 1024, {
  _: PitchDetectionResult, e: AudioEvent ->
})