package com.isoshi_moustache.wear

import android.util.Log
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.json.JSONObject

class ThingsBoardProxy {
    companion object {
        const val THINS_BOARD_URL_BASE: String = "http://52.198.79.19"
        const val DEVICE_TOKEN: String = "sguEfFFpJhK7rABnaWpB"
    }

    fun pushLog(json: JSONObject) {
        val data = json.toString()
        Log.i("isoshi-moustache", data)
        val request = "$THINS_BOARD_URL_BASE/api/v1/$DEVICE_TOKEN/telemetry".httpPost().body(data)
        request.appendHeader("Content-Type", "application/json")
        request.response { _, _, result ->
            when (result) {
                is Result.Success -> {
                    Log.i("isoshi-moustache", "http success")
                }
                is Result.Failure -> {
                    Log.e("isoshi-moustache", "http error")
                }
            }
        }
    }
}
