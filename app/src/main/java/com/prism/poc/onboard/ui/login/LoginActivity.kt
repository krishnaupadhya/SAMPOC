package com.prism.poc.onboard.ui.login

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.prism.poc.*
import com.prism.poc.R


class LoginActivity : AppCompatActivity() {


    private val REQUEST_CHECK_SETTINGS: Int=100001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)


        login.setOnClickListener {
            when {
                TextUtils.isEmpty(username.text) -> {
                    showLoginFailed(R.string.invalid_username)
                }
                TextUtils.isEmpty(password.text) -> {
                    showLoginFailed(R.string.invalid_password)
                }
                else -> {
                    GenericUtil.putInSharedPreferences(AppConstants.USER_NAME, username.text.toString())
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder?.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val  states = LocationSettingsStates.fromIntent(intent)
        when (requestCode) {
             REQUEST_CHECK_SETTINGS ->
                when (resultCode){
                    Activity.RESULT_OK ->  onStartResolutionForResultSuccess()
                    Activity.RESULT_CANCELED->onStartResolutionForResultFailed()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onStartResolutionForResultFailed() {
        TODO("Not yet implemented")
    }

    private fun onStartResolutionForResultSuccess() {
        TODO("Not yet implemented")
    }
}

