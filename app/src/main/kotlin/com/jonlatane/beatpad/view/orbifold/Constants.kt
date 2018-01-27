package com.jonlatane.beatpad.view.orbifold

import android.view.View
import com.jonlatane.beatpad.util.density

internal val ANIMATION_DURATION: Long = 200
internal val CENTRAL_CHORD_SCALE = 2.2f
internal val HALF_STEP_SCALE = 0.7f

internal val View.axisElevation get() = 3f * density
internal val View.connectorElevation get() = 4f * density
internal val View.defaultChordElevation get() = 5f * density
internal val View.halfStepBackgroundElevation get() = 7f * density
internal val View.halfStepChordElevation get() = 8f * density
internal val View.centralBackgroundElevation get() = 11f * density
internal val View.centralChordElevation get() = 12f * density