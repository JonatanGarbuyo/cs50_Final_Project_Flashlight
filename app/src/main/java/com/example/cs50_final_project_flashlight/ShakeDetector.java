package com.example.cs50_final_project_flashlight;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// http://jasonmcreynolds.com/?p=388
public class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    //private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_SLOP_TIME_MS = 100;
    //private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 600;

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        void onShake(int count);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            float x = event.values[0];
            // float y = event.values[1];
            // float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            // float gY = y / SensorManager.GRAVITY_EARTH;
            // float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            // float gForce = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);
            // float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
            float gForce = (float) Math.sqrt(gX * gX);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                mListener.onShake(mShakeCount);
            }
        }
    }
}