package com.mateworld.gnssreceivertracker

import android.provider.Settings
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val locationsRef = database.getReference("locations")

    fun updateLocation(latitude: Double, longitude: Double, timestamp: Long, dateTime: String) {
        try {
            val locationData = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to timestamp,
                "dateTime" to dateTime // Use the passed dateTime parameter directly
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

    fun getCurrentLocation(onLocationReceived: (Double, Double) -> Unit) {
        locationsRef.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (locationSnapshot in snapshot.children) {
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                    onLocationReceived(latitude, longitude)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
                error.toException().printStackTrace()
            }
        })
    }
}
