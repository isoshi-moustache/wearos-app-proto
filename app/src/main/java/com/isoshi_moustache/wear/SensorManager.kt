package com.isoshi_moustache.wear

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.wearable.activity.WearableActivity
import android.util.Log

class SensorManager constructor(_context: Activity) : SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private val mContext: Activity = _context

    var LatestStepCounter: Float? = null
    var LatestHeartRate: Float? = null
    var LatestGyro: Triple<Float, Float, Float>? = null
    var LatestAcceleroMeter: Triple<Float, Float, Float>? = null

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            this.LatestHeartRate = event.values[0]
        } else if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            this.LatestAcceleroMeter = Triple(event.values[0], event.values[1], event.values[2])
        } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            this.LatestGyro = Triple(event.values[0], event.values[1], event.values[2])
        } else if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            this.LatestStepCounter = event.values[0]
        }
    }

    fun start() {
        if (this.mSensorManager != null) return

        this.mSensorManager = this.mContext.getSystemService(WearableActivity.SENSOR_SERVICE) as SensorManager

        val sensorTypeList = listOf(
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_STEP_COUNTER
        )

        sensorTypeList.forEach {
            val sensor = this.mSensorManager!!.getDefaultSensor(it)
            if (sensor == null) {
                Log.w("isoshi-moustache", "$it not found")
            } else {
                this.mSensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }

    }

    fun stop() {
        if (this.mSensorManager == null) return
        this.mSensorManager!!.unregisterListener(this)
        this.mSensorManager = null
    }

}