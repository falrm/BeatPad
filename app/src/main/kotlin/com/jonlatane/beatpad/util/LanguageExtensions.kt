package com.jonlatane.beatpad.util

import android.support.v7.widget.RecyclerView
import com.jonlatane.beatpad.util.smartrecycler.viewHolders

infix fun <T: Any> T?.but(other: T): T = this ?: other

fun <T> Iterable<T>.applyToEach(
  mutation: T.() -> Unit
) = forEach { it.mutation() }
fun <T> Iterable<T>.applyToEachIndexed(
  mutation: T.(Int) -> Unit
) = forEachIndexed { index, it -> it.mutation(index) }