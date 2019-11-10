package com.jonlatane.beatpad.util

import java.util.concurrent.atomic.AtomicInteger

fun AtomicInteger.incrementUntil(targetCount: Int, then: () -> Unit): () -> Unit = {
  if(incrementAndGet() == targetCount) then()
}

fun incrementUntil(targetCount: Int, then: () -> Unit): () -> Unit {
  val lock = AtomicInteger(0)
  return lock.incrementUntil(targetCount, then)
}