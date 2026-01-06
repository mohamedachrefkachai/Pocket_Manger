package com.example.recyclersleam.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class movement implements SensorEventListener {

    // Seuil de changement d'accélération (Delta) pour détecter une secousse
    private static final float SHAKE_THRESHOLD_DELTA = 0.5F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    // FLIP Thresholds
    private static final float FLIP_FACE_DOWN_THRESHOLD = -8.0F;
    private static final float FLIP_FACE_UP_THRESHOLD = 8.0F;

    private OnShakeListener mShakeListener;
    private OnFlipListener mFlipListener;

    private long mShakeTimestamp;
    private int mShakeCount;

    private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
    private boolean mFirstUpdate = true;
    private boolean mIsFaceDown = false;

    public interface OnShakeListener {
        void onShake(int count);
    }

    public interface OnFlipListener {
        void onFlip(boolean isFaceDown);
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.mShakeListener = listener;
    }

    public void setOnFlipListener(OnFlipListener listener) {
        this.mFlipListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // --- FLIP DETECTION ---
        if (mFlipListener != null) {
            if (z < FLIP_FACE_DOWN_THRESHOLD && !mIsFaceDown) {
                mIsFaceDown = true;
                mFlipListener.onFlip(true);
            } else if (z > FLIP_FACE_UP_THRESHOLD && mIsFaceDown) {
                mIsFaceDown = false;
                mFlipListener.onFlip(false);
            }
        }

        // --- SHAKE DETECTION ---
        if (mShakeListener != null) {
            if (mFirstUpdate) {
                mLastX = gX;
                mLastY = gY;
                mLastZ = gZ;
                mFirstUpdate = false;
                return;
            }

            float deltaX = Math.abs(mLastX - gX);
            float deltaY = Math.abs(mLastY - gY);
            float deltaZ = Math.abs(mLastZ - gZ);

            float segment = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (segment > SHAKE_THRESHOLD_DELTA) {
                final long now = System.currentTimeMillis();
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                mShakeListener.onShake(mShakeCount);
            }

            mLastX = gX;
            mLastY = gY;
            mLastZ = gZ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }
}
