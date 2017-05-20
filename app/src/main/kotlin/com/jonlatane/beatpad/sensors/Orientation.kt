package com.jonlatane.beatpad.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import org.jetbrains.anko.AnkoLogger

/**
 * Created by jonlatane on 5/5/17.
 */

object Orientation: AnkoLogger {
    var azimuth = 0f
    var pitch = 0f
    var roll = 0f
    var inclination = 0f

    fun initialize(c: Context) {
        val sensorManager = c.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotationMatrix = FloatArray(16)
        val orientationValues = FloatArray(3)
        sensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)
                    azimuth = orientationValues[0]
                    pitch = orientationValues[1]
                    roll = orientationValues[2]
                    inclination = SensorManager.getInclination(rotationMatrix)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }, sensor, 50000)
    }

    /**
     * Device pitch as a float between 0 and 1.  Only really allows tilts up to +/-75 degrees above/below
     * the ground.
     * @return
     */
    fun normalizedDevicePitch(): Float {
        val normalizedPitch = Math.max(0f, Math.min(1f, (1.58f - pitch * 1.2f) / 3.14f))
        return normalizedPitch
    }

    /**
     * Device roll as a float between -0.5 (left) and 0.5 (right)
     * @return
     */
    fun normalizedDeviceRoll(): Float {
        var relativeRoll = roll
        while (relativeRoll > 1.62) relativeRoll -= 1.62f
        while (relativeRoll < -1.62) relativeRoll += 1.62f
        relativeRoll /= 3.14f
        return relativeRoll
    }
}
