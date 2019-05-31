package com.tykesoft.trackcontainers.activity

import android.content.DialogInterface
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.maps.android.ui.IconGenerator
import com.tykesoft.trackcontainers.R
import com.tykesoft.trackcontainers.model.Container
import kotlin.collections.HashMap


class OperationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mRef: DatabaseReference

    private val containerMarkerMap: HashMap<Marker, Container> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operation)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mRef = FirebaseDatabase.getInstance().reference

        val containerListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Database Error", databaseError.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                loadContainers(dataSnapshot)
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

        mMap.setOnMarkerClickListener {
            val container = containerMarkerMap[it]

            val builder = AlertDialog.Builder(this)
            builder.setTitle(container?.containerId)
            builder.setMessage("${container?.sensorId}")
            builder.setPositiveButton("OK", DialogInterface.OnClickListener(okButtonClick))
            builder.setNegativeButton("Relocate", DialogInterface.OnClickListener(relocateButtonClick))
            builder.show()

            true
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.01, 28.97), 5f))
    }

    private val relocateButtonClick = { dialogInterface: DialogInterface, which: Int ->

    }

    private val okButtonClick = { dialogInterface: DialogInterface, which: Int ->

    }

    private fun loadContainers(dataSnapshot: DataSnapshot) {
        val containers = dataSnapshot.children.iterator()

        while(containers.hasNext()) {
            val containerData = containers.next().value as HashMap<*, *>
            val container = Container(
                    containerData["containerId"].toString(),
                    containerData["sensorId"].toString().toInt(),
                    containerData["lat"].toString().toDouble(),
                    containerData["long"].toString().toDouble(),
                    containerData["temperature"].toString().toDouble(),
                    containerData["rate"].toString().toInt())
            addToMap(container)
        }
    }

    private fun addToMap(container: Container) {
        val position = LatLng(container.lat!!, container.long!!)

        val iconGenerator = IconGenerator(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iconGenerator.setBackground(getDrawable(R.drawable.container))
            iconGenerator.setContentPadding(55, 70, 0, 0)
        }
        iconGenerator.setTextAppearance(R.style.mapMarkerLabelText)

        val marker = mMap.addMarker(MarkerOptions()
                .position(position)
                .title("${container.temperature}")
                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${container.rate}%"))))

        containerMarkerMap[marker] = container
    }

}
