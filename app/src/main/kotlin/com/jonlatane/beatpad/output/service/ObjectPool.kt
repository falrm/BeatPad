package com.jonlatane.beatpad.output.service

import java.util.Vector

/**
 * Simple object pool using inline functions. Minimal fuss; simply reserve and make sure to release.
 *
 * Example usage:
 *
 * `val pool = ObjectPool of { MyClass() }; val obj = pool.reserve(); pool.release(obj)`
 */
class ObjectPool<T>(
	private inline val konstructor: () -> T,
	initialSize: Int = 16
) {
	companion object {
		infix fun <O> of(konstructor: () -> O) = ObjectPool(konstructor)
		private const val used = true
		private const val available = false
	}
	private var currentIndex = 0
	private val objects = Vector<T>(initialSize)
	private val usages = Vector<Boolean>()

	init {
		for (i in 1..initialSize) {
			objects += konstructor()
		}
	}

	fun reserve(): T {
		var currentObject = objects[currentIndex]
		while (usages[currentIndex++] == used) {
			if (currentIndex >= objects.size) {
				objects.add(konstructor())
				usages.add(available)
			}
			currentObject = objects[++currentIndex]
		}
		return currentObject
	}
	fun release(obj: T) {
		usages[objects.indexOf(obj)] = available
	}
}