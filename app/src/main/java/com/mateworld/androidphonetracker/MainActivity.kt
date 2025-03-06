package com.mateworld.gnssreceivertracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.mateworld.androidphonetracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isTracking = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1234
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 5678
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnStartTracking.setOnClickListener {
            if (!isTracking) {
                if (checkLocationPermissions()) {
                    // Start the location tracking service
                    Intent(this, LocationService::class.java).also { intent ->
                        startService(intent)
                        isTracking = true
                        updateTrackingUI()
                        showMessage("Location tracking started")
                    }
                } else {
                    requestLocationPermissions()
                }
            }
        }

        binding.btnStopTracking.setOnClickListener {
            if (isTracking) {
                stopLocationTracking()
            }
        }
    }

    private fun stopLocationTracking() {
        Intent(this, LocationService::class.java).also { intent ->
            stopService(intent)
            isTracking = false
            updateTrackingUI()
            showMessage("Location tracking stopped")
        }
    }

    private fun updateTrackingUI() {
        binding.btnStartTracking.isEnabled = !isTracking
        binding.btnStopTracking.isEnabled = isTracking
        binding.tvStatus.text = if (isTracking) "Tracking Active" else "Tracking Inactive"
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && backgroundLocation
    }

    private fun requestLocationPermissions() {
        // Request fine location permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If fine location is granted, request background location
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission()
                    } else {
                        startLocationTracking()
                    }
                } else {
                    showMessage("Location permission is required")
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationTracking()
                } else {
                    showMessage("Background location permission is required for tracking")
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
