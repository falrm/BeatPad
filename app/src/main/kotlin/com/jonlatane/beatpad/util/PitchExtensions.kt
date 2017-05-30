package com.jonlatane.beatpad.util

/**
 * Created by jonlatane on 5/26/17.
 */
val Int.pitchClass get() = (1200 + this) % 12