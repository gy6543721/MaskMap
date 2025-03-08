package com.levilin.maskmap

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MapInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {

    private var mWindow: View = View.inflate(context, R.layout.map_info_window, null)

    private fun render(marker: Marker, view: View) {

        val storeName = view.findViewById<TextView>(R.id.textStoreName)
        val adultMaskQuantity = view.findViewById<TextView>(R.id.textAdultMaskQuantity)
        val childMaskQuantity = view.findViewById<TextView>(R.id.textChildMaskQuantity)

        val mask = marker.snippet?.toString()?.split(",")

        storeName.text = marker.title
        adultMaskQuantity.text = mask?.get(0)
        childMaskQuantity.text = mask?.get(1)

    }

    override fun getInfoContents(marker: Marker): View {
        render(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}