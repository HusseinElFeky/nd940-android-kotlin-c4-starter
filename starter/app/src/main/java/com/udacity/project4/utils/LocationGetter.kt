package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationGetter(private val context: Context) {

    private val flpClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager =
            context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun checkDeviceLocationSettings(
        fragment: Fragment,
        resolve: Boolean = true,
        onSuccessCallback: (() -> Unit)? = null,
        onFailureCallback: (() -> Unit)? = null
    ) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(fragment.requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnSuccessListener {
            onSuccessCallback?.invoke()
        }

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                        checkDeviceLocationSettings(
                            fragment,
                            false,
                            onSuccessCallback,
                            onFailureCallback
                        )
                    }.launch(IntentSenderRequest.Builder(exception.resolution.intentSender).build())
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                onFailureCallback?.invoke()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(locationListener: (Location) -> Unit) {
        flpClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            if (location == null) {
                requestNewLocationData(locationListener)
            } else {
                locationListener.invoke(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(locationListener: (Location) -> Unit) {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationListener.invoke(locationResult.lastLocation)
            }
        }

        with(LocationRequest()) {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
            flpClient.requestLocationUpdates(this, locationCallback, Looper.myLooper())
        }
    }

    companion object {
        private val TAG = LocationGetter::class.java.simpleName
    }
}
