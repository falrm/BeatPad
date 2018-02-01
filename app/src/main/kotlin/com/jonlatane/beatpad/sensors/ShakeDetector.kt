package com.jonlatane.beatpad.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

object ShakeDetector : SensorEventListener {
	fun initialize(c: Context) {
		val sensorManager = c.getSystemService(Context.SENSOR_SERVICE) as SensorManager
		val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
		sensorManager.registerListener(this, sensor, 50000)
	}

	var onShakeListener: OnShakeListener? = null

	override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

	override fun onSensorChanged(event: SensorEvent) {
		val values = event.values
		if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
		val now = System.currentTimeMillis()

		if (now - mLastForce > SHAKE_TIMEOUT) {
			mShakeCount = 0
		}

		if (now - mLastTime > TIME_THRESHOLD) {
			val diff = now - mLastTime
			val speed = Math.abs(values[0] + values[1] + values[2] - mLastX - mLastY - mLastZ) / diff * 10000
			if (speed > FORCE_THRESHOLD) {
				if (++mShakeCount >= SHAKE_COUNT && now - mLastShake > SHAKE_DURATION) {
					mLastShake = now
					mShakeCount = 0
					if (onShakeListener != null) {
						onShakeListener!!.onShake()
					}
				}
				mLastForce = now
			}
			mLastTime = now
			mLastX = values[0]
			mLastY = values[1]
			mLastZ = values[2]
		}
	}

	interface OnShakeListener {
		fun onShake()
	}

	private var mLastX = -1.0f
	private var mLastY = -1.0f
	private var mLastZ = -1.0f
	private var mLastTime: Long = 0
	private var mShakeCount = 0
	private var mLastShake: Long = 0
	private var mLastForce: Long = 0

	private const val FORCE_THRESHOLD = 500
	private const val TIME_THRESHOLD = 100
	private const val SHAKE_TIMEOUT = 500
	private const val SHAKE_DURATION = 1000
	private const val SHAKE_COUNT = 10
}