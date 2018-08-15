package com.jonlatane.beatpad.util

infix fun <T: Any> T?.but(other: T): T = this ?: other