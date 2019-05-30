package com.tykesoft.trackcontainers.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.tykesoft.trackcontainers.R

class OperationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operation)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mRef = FirebaseDatabase.getInstance().reference

        val containerListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.e("Database Error", databaseError.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                loadContainers(dataSnapshot!!)
            }

        }

        mRef.child("containers").addValueEventListener(containerListener)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun loadContainers(dataSnapshot: DataSnapshot) {
        val containers = dataSnapshot.children.iterator()

        while(containers.hasNext()) {
            val container = containers.next().value as HashMap<*, *>
            addToMap(container)
        }
    }

    private fun addToMap(container: HashMap<*, *>) {
        val lat = container["lat"].toString().toDouble()
        val long = container["long"].toString().toDouble()
        val temperature = container["temperature"]
        val rate = container["rate"]
        val position = LatLng(lat, long)
        mMap.addMarker(MarkerOptions()
                .position(position)
                .title("$temperature")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.container)))
    }
}
