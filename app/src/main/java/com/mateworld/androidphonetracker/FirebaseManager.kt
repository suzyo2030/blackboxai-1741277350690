package com.mateworld.gnssreceivertracker

import android.provider.Settings
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val locationsRef = database.getReference("locations")

    fun updateLocation(latitude: Double, longitude: Double, timestamp: Long) {
        try {
            val locationData = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to timestamp,
                "dateTime" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(timestamp))
            )

            // Create a unique key for each location update
            val key = locationsRef.push().key
            key?.let {
                locationsRef.child(it).setValue(locationData)
                    .addOnSuccessListener {
                        // Location updated successfully
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                        e.printStackTrace()
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
