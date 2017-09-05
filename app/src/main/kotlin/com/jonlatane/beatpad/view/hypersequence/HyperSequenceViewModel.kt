package com.jonlatane.beatpad.view.hypersequence

import android.view.View
import com.jonlatane.beatpad.model.ToneSequence
import com.jonlatane.beatpad.storage.ToneSequenceStorage
import com.jonlatane.beatpad.view.NonDelayedHorizontalScrollView
import com.jonlatane.beatpad.view.NonDelayedScrollView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import com.jonlatane.beatpad.view.tonesequence.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates.observable

class HyperSequenceViewModel {
	val toneSequence: ToneSequenceViewModel = ToneSequenceViewModel()
	val sequencerThread get() = toneSequence.sequencerThread
}