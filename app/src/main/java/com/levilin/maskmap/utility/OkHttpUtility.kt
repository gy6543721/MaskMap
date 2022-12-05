package com.levilin.maskmap.utility

import okhttp3.*
import okio.IOException

class OkHttpUtility {
    private var mOkHttpClient: OkHttpClient? = null

    companion object {
        val mOkHttpUtility: OkHttpUtility by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            OkHttpUtility()
        }
    }

    init {
        //Part 1: Set OkHttpClient
        mOkHttpClient = OkHttpClient().newBuilder().build()
    }

    //Get Async Update of Data
    fun getAsync(url: String, callback: AsyncCallback) {
        //Part 2: Set Request to the URL
        val request = with(Request.Builder()) {
            url(url)
            get()
            build()
        }

        //Part 3: Set Call
        val call = mOkHttpClient?.newCall(request)

        // Get enqueue Async response
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                callback.onResponse(response)
            }
        })
    }

    interface AsyncCallback {
        fun onResponse(response: Response)
        fun onFailure(e: IOException)
    }
}