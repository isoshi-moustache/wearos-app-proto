package com.isoshi_moustache.wear

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import org.json.JSONObject
import java.util.*


class MainActivity : WearableActivity() {


    private val mTimerTask = object : TimerTask() {
        override fun run() {
            try {
                val connManager =
                    applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connManager.activeNetworkInfo
                //
                var status = "!!未接続!!\n"
                if (networkInfo != null && networkInfo.isConnected) {
                    status = "接続済\n"
                    val json = JSONObject()
                    json.put("step_counter", mSensorManager.LatestStepCounter)
                    json.put("heart_rate", mSensorManager.LatestHeartRate)
                    json.put("beacon", mBeaconReciever.BeaconList.joinToString { e -> e })
                    if (mSensorManager.LatestGyro != null) {
                        json.put("gx", mSensorManager.LatestGyro!!.first)
                        json.put("gy", mSensorManager.LatestGyro!!.second)
                        json.put("gz", mSensorManager.LatestGyro!!.third)
                    }
                    if (mSensorManager.LatestAcceleroMeter != null) {
                        json.put("ax", mSensorManager.LatestAcceleroMeter!!.first)
                        json.put("ay", mSensorManager.LatestAcceleroMeter!!.second)
                        json.put("az", mSensorManager.LatestAcceleroMeter!!.third)
                    }
                    mThingsBoard.pushLog(json)
                }
                status += "歩数：${mSensorManager.LatestStepCounter}\n"
                status += "心拍：${mSensorManager.LatestHeartRate}\n"
                if (mSensorManager.LatestGyro != null) {
                    val x = String.format("%.3f", mSensorManager.LatestGyro!!.first)
                    val y = String.format("%.3f", mSensorManager.LatestGyro!!.second)
                    val z = String.format("%.3f", mSensorManager.LatestGyro!!.third)
                    status += "ジャイロ：$x,$y,$z\n"
                } else {
                    status += "ジャイロ：\n"
                }
                if (mSensorManager.LatestAcceleroMeter != null) {
                    val x = String.format("%.3f", mSensorManager.LatestAcceleroMeter!!.first)
                    val y = String.format("%.3f", mSensorManager.LatestAcceleroMeter!!.second)
                    val z = String.format("%.3f", mSensorManager.LatestAcceleroMeter!!.third)

                    status += "加速度：$x,$y,$z\n"
                } else {
                    status += "加速度：\n"
                }
                status += "\n--- Beacon ---\n"
                if (mBeaconReciever.BeaconList.count() > 0) {
                    mBeaconReciever.BeaconList.forEach {
                        status += "$it\n"
                    }
                }
                mHandler.post {
                    mTxtWifiStatus!!.text = status
                }
                mBeaconReciever.BeaconList.clear()
            } catch (e: Throwable) {
                mHandler.post {
                    mTxtWifiStatus!!.text = "error"
                }
            }

        }
    }

    private val mContext = this
    private val mHandler = Handler()
    private val mSensorManager = SensorManager(this)
    private val mBeaconReciever = BeaconReceiver(this)
    private val mThingsBoard = ThingsBoardProxy()

    // private val UUID = "74A23A96-A479-4330-AEFF-2421B6CF443C"
    // private val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"

    private var mTxtWifiStatus: TextView? = null
    private val mTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        this.mTxtWifiStatus = this.findViewById(R.id.txtWifiStatus)

        // Enables Always-on
        setAmbientEnabled()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            }
            if (this.checkSelfPermission(android.Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 2)
            }
        }

        Log.w("isoshi-moustache", "[END]onCreate")
    }

    override fun onPause() {
        super.onPause()
        this.mTimer.cancel()
        this.mBeaconReciever.stop()
        this.mSensorManager.stop()
    }

    override fun onResume() {
        super.onResume()
        this.mSensorManager.start()
        this.mBeaconReciever.start()
        this.mTimer.schedule(this.mTimerTask, 0, 1000)

        /*
        var beacon = Beacon.Builder()
            .setId1(UUID)
            .setId2("1")
            .setId3("1")
            .setManufacturer(0x004C)
            .build()

        val beaconParser = BeaconParser().setBeaconLayout(IBEACON_FORMAT)

        val beaconTransmitter = BeaconTransmitter(applicationContext, beaconParser)
        beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                Log.d("isoshi-moustache", "SUCCESS")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.w("isoshi-moustache", "FAILURE")
            }
        })
        */
        Log.w("isoshi-moustache", "[END]onResume")
    }


}
