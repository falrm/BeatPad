package com.jonlatane.beatpad.util

import android.content.Context
import android.os.Build

fun Context.color(resId: Int) =
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		resources.getColor(resId, theme)
	else @Suppress("Deprecated")
	  resources.getColor(resId)