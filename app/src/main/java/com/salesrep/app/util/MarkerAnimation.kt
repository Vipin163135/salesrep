package com.salesrep.app.util

import android.os.Handler
import android.os.SystemClock
import android.os.WorkSource
import com.google.android.gms.maps.model.LatLng
import com.salesrep.app.util.LatLngInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil

object MarkerAnimation {
    fun animateMarkerToGB(
        marker: Marker,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition = marker.position
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator: Interpolator = LinearInterpolator()
        val durationInMs = 2000f
        handler.post(object : Runnable {
            var elapsed: Long = 0
            var t = 0f
            var v = 0f
            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                marker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition)

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    fun calculateBearing(latlgnSource: LatLng,latlgnDest: LatLng): Float {
        val sourceLatLng = latlgnSource
        val destinationLatLng = latlgnDest
        return SphericalUtil.computeHeading(sourceLatLng, destinationLatLng).toFloat()
    }

}