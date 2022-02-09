package com.isoshi_moustache.wear

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import org.altbeacon.beacon.*

class BeaconReceiver constructor(_context: Activity) : BeaconConsumer {
    companion object {
        const val IBEACON_FORMAT: String = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
    }

    private val mUuid = Identifier.parse("fda50693-a4e2-4fb1-afcf-c6eb07647825")
    private val mContext: Activity = _context
    private var mBeaconManager: BeaconManager? = null
    var BeaconList: MutableList<String> = mutableListOf()

    override fun getApplicationContext(): Context {
        return this.mContext.applicationContext
    }

    override fun unbindService(conn: ServiceConnection) {
        this.mContext.unbindService(conn)
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flag: Int): Boolean {
        return this.mContext.bindService(service, conn, flag)
    }

    override fun onBeaconServiceConnect() {
        Log.i("isoshi-moustache", "connected")
        val mRegion = Region("ibeacon", this.mUuid, null, null)

        this.mBeaconManager?.removeAllMonitorNotifiers()
        this.mBeaconManager?.removeAllRangeNotifiers()
        this.mBeaconManager!!.addMonitorNotifier(this.mMonitorNotifier)
        this.mBeaconManager!!.addRangeNotifier(this.mRangeNotifier)
        this.mBeaconManager!!.startMonitoringBeaconsInRegion(mRegion)
    }

    fun start() {
        if (this.mBeaconManager != null) return

        this.mBeaconManager = BeaconManager.getInstanceForApplication(this.mContext)
        this.mBeaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))
        this.mBeaconManager!!.bind(this)
    }

    fun stop() {
        if (this.mBeaconManager == null) return
        this.mBeaconManager!!.unbind(this)
        this.mBeaconManager = null
    }

    /**
     * MonitorNotifier
     */
    private val mMonitorNotifier = object : MonitorNotifier {
        override fun didEnterRegion(region: Region) {
            // 領域進入時に実行
            Log.d("isoshi-moustache", "didEnterRegion")
        }

        override fun didExitRegion(region: Region) {
            // 領域退出時に実行
            Log.d("isoshi-moustache", "didExitRegion")
        }

        override fun didDetermineStateForRegion(i: Int, region: Region) {
            // 領域への侵入/退出のステータスが変化したときに実行
            Log.d("isoshi-moustache", "didDetermineStateForRegion i = " + i)
            try {
                if (i == 1) {
                    mBeaconManager?.startRangingBeaconsInRegion(region)
                } else {
                    mBeaconManager?.stopRangingBeaconsInRegion(region)
                }
            } catch (e: Throwable) {
                Log.d("isoshi-moustache", "didDetermineStateForRegion e = " + e.message)
            }

        }
    }

    /**
     * RangeNotifier
     */
    private val mRangeNotifier = object : RangeNotifier {
        override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
            Log.d("isoshi-moustache", "didRangeBeaconsInRegion")
            for (beacon in beacons) {
                Log.d(
                    "isoshi-moustache", "UUID:" + beacon.id1 + ", major:"
                            + beacon.id2 + ", minor:" + beacon.id3 + ", RSSI:"
                            + beacon.rssi + ", TxPower:" + beacon.txPower
                            + ", Distance:" + beacon.distance
                )
                if (BeaconList.indexOf(beacon.id1.toString()) < 0) {
                    BeaconList.add(beacon.id3.toString())
                }
            }
        }
    }
}