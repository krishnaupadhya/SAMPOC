package com.prism.poc

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prism.poc.AppConstants.REQUEST_CHECK_SETTINGS_GPS
import com.prism.poc.events.LocationPostEvent
import com.prism.poc.events.LocationWorker
import com.prism.poc.locationhistory.LocationHistoryUtils
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
        UPDATE_INTERVAL_IN_MILLISECONDS / 2

    /**
     * The current location.
     */
    private var mCurrentLocation: Location? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for changes in location.
     */
    private var mLocationCallback: LocationCallback? = null

    // boolean flag to toggle the ui
    private var mRequestingLocationUpdates: Boolean = false

    // bunch of location related apis
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var requestGpsPermission = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        intiLocationManager()
    }

    private fun intiLocationManager() {
        init()
        initLocationWithCheckPermission()
    }


    private fun initLocationWithCheckPermission() {
        val permissionAccessCoarseLocationApproved =
            (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)
        if (permissionAccessCoarseLocationApproved) {
            var backgroundLocationPermissionApproved =
                true //background location permission not applicable for below android10(Q) devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLocationPermissionApproved = (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                        == PackageManager.PERMISSION_GRANTED)
            }
            if (backgroundLocationPermissionApproved) {
                onLocationSettingsSuccess()
            } else {
                // App can only access location in the foreground. Display a dialog
                // warning the user that your app must have all-the-time access to
                // location in order to function properly. Then, request background
                // location.
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    AppConstants.PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    AppConstants.PERMISSION_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    AppConstants.PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun onLocationSettingsSuccess() {
        startLocationUpdates()
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == AppConstants.PERMISSION_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                GenericUtil.log("User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onLocationSettingsSuccess()
            } else {
                // permission
                GenericUtil.showCustomAlertDialogBtnClick(
                    this,
                    getString(R.string.permission_rationale),
                    getString(R.string.permission_denied_explanation)
                ) { dialog: DialogInterface, which: Int ->
                    GenericUtil.showAppDetailOnPermissionDenied(this)
                    dialog.dismiss()
                }
            }
        }
    }



    override fun onRestart() {
        super.onRestart()
        requestGpsPermission = true
        intiLocationManager()
    }

    override fun onStop() {
        stopLocationUpdates()
        super.onStop()
    }


    private fun stopLocationUpdates() {
        // Removing location updates
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(
                    this
                ) {
                    GenericUtil.log(" removeLocationUpdates success")
                    mRequestingLocationUpdates = false
                }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment =
            supportFragmentManager.findFragmentById(R.id.container)
        fragment?.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS_GPS -> when (resultCode) {
                Activity.RESULT_OK -> onGpsSettingSuccess()
                Activity.RESULT_CANCELED ->                         // permission
                    GenericUtil.showCustomAlertDialog(
                        this,
                        getString(R.string.gps_network_not_enabled),
                        getString(R.string.gps_rationale_denied_explanation)
                    )
                else -> {
                }
            }
            else -> {
            }
        }
    }

    private fun onGpsSettingSuccess() {
        onLocationSettingsSuccess()
        requestGpsPermission = false
    }


    private fun init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // location is received
                if (mRequestingLocationUpdates) {
                    mCurrentLocation = locationResult.lastLocation
                    mCurrentLocation?.let {
                        if (!GenericUtil.isDistanceWithinRange(mCurrentLocation)) {
                            LocationHistoryUtils.instance?.saveLocationToLocal(mCurrentLocation)
                        }
                        EventBus.getDefault().post(LocationPostEvent(it.latitude, it.longitude))
                    }
                    stopLocationUpdates()
                    startLocationService()
                }
            }
        }
        mLocationRequest = LocationRequest()
        mLocationRequest?.let {
            it.interval = UPDATE_INTERVAL_IN_MILLISECONDS
            it.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            it.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(it)
            mLocationSettingsRequest = builder.build()
        }
    }

    private fun startLocationService() {
        GenericUtil.updateLocationBackGroundService(this)
        initLocationWorker()
    }

    fun initLocationWorker() {
        val periodicWorkBuilder = PeriodicWorkRequest.Builder(
            LocationWorker::class.java,
            AppConstants.DEFAULT_TIME_TO_CHECK_LOCATION,
            TimeUnit.MINUTES
        )
        val myWork = periodicWorkBuilder
            .setInitialDelay(
                AppConstants.DEFAULT_TIME_TO_CHECK_LOCATION,
                TimeUnit.MINUTES
            )
            .build()
        Objects.requireNonNull(WorkManager.getInstance(this))
            .enqueueUniquePeriodicWork(
                AppConstants.LOCATION_WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                myWork
            )
    }

    private fun startLocationUpdates() {
        mSettingsClient
            ?.checkLocationSettings(mLocationSettingsRequest)
            ?.addOnSuccessListener(
                this,
                OnSuccessListener<LocationSettingsResponse> { locationSettingsResponse: LocationSettingsResponse? ->
                    GenericUtil.log("All location settings are satisfied- requestLocationUpdates CALLED")
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                    } else {
                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback, Looper.myLooper()
                        )
                        mRequestingLocationUpdates = true
                    }

                }
            )
            ?.addOnFailureListener(this, OnFailureListener { e: Exception ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        GenericUtil.log(
                            "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings "
                        )
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this,
                                REQUEST_CHECK_SETTINGS_GPS
                            )
                        } catch (sie: SendIntentException) {
                            GenericUtil.log("PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                        GenericUtil.handleException(e)
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                    }
                }
            })
    }
}