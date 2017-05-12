package com.jonlatane.beatpad.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by jonlatane on 5/5/17.
 */

public class Orientation {
    private static final String TAG = Orientation.class.getSimpleName();
    public static Float azimuth = 0f;
    public static Float pitch = 0f;
    public static Float roll = 0f;
    public static float inclination = 0f;

    public static void initialize(Context c) {
        SensorManager sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        final float[] rotationMatrix = new float[16];
        final float[] orientationValues = new float[3];
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                    SensorManager.getOrientation(rotationMatrix, orientationValues);
                    azimuth = orientationValues[0];
                    pitch = orientationValues[1];
                    roll = orientationValues[2];
                    inclination = SensorManager.getInclination(rotationMatrix);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        }, sensor, 10000);
    }

    /**
     * Device pitch as a float between 0 and 1.  Only really allows tilts up to +/-75 degrees above/below
     * the ground.
     * @return
     */
    public static float normalizedDevicePitch() {
        float normalizedPitch = Math.max(0f, Math.min(1f, (1.58f - pitch * 1.2f) / 3.14f));
        return normalizedPitch;
    }

    /**
     * Device roll as a float between -0.5 (left) and 0.5 (right)
     * @return
     */
    public static float normalizedDeviceRoll() {
        float relativeRoll = roll;
        while(relativeRoll > 1.62) relativeRoll -= 1.62;
        while(relativeRoll <-1.62) relativeRoll += 1.62;
        relativeRoll /= 3.14;
        return relativeRoll;
    }
}
