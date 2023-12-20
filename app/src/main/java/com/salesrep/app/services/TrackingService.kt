package com.salesrep.app.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.salesrep.app.R
import com.salesrep.app.dao.UserTrackDao
import com.salesrep.app.data.models.TrackTemplate
import com.salesrep.app.data.models.UserTrackTemplate
import com.salesrep.app.ui.home.MainActivity
import com.salesrep.app.util.*
import com.salesrep.app.util.TrackConstants.ACTION_PAUSE_SERVICE
import com.salesrep.app.util.TrackConstants.ACTION_SHOW_TRACKING_FRAGMENT
import com.salesrep.app.util.TrackConstants.ACTION_START_OR_RESUME_SERVICE
import com.salesrep.app.util.TrackConstants.ACTION_STOP_SERVICE
import com.salesrep.app.util.TrackConstants.FASTEST_LOCATION_INTERVAL
import com.salesrep.app.util.TrackConstants.LOCATION_UPDATE_INTERVAL
import com.salesrep.app.util.TrackConstants.NOTIFICATION_CHANNEL_ID
import com.salesrep.app.util.TrackConstants.NOTIFICATION_CHANNEL_NAME
import com.salesrep.app.util.TrackConstants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.toImmutableList
import timber.log.Timber
import javax.inject.Inject

typealias Polylines = MutableList<TrackTemplate>


@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var routeId: Int = -1

    @Inject
    lateinit var userTrackDao: UserTrackDao

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            routeId=   it.extras?.getInt(DataTransferKeys.KEY_ROUTE_ID) ?: -1

            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            val request = LocationRequest().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_LOCATION_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)

            pathPoints.value?.apply {

                this.add(TrackTemplate(
                    pos,
                    DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                ))
                pathPoints.postValue(this)
            }

            lifecycleScope.launchWhenCreated {
                userTrackDao.insert(
                    UserTrackTemplate(
                        id = routeId,
                        trackLocations = pathPoints.value?.toImmutableList()!!
                    )
                )
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        pathPoints.postValue(mutableListOf())
    } ?: pathPoints.postValue(mutableListOf())

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_nav)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.tracking_location))
            .setContentIntent(getMainActivityPendingIntent())
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_MUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}

