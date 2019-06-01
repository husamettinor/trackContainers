package com.tykesoft.trackcontainers.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.google.firebase.database.*
import com.google.maps.android.ui.IconGenerator
import com.tykesoft.trackcontainers.R
import com.tykesoft.trackcontainers.model.Container
import java.lang.Math.abs
import java.util.*
import kotlin.collections.HashMap


class OperationActivity : AppCompatActivity(), OnMapReadyCallback {

    private val RELOCATE_REQUEST = 1
    private val DEFAULT_ZOOM = 7.0F
    private val ZOOM_RESOLUTION = 1.2F
    private val MAX_MARKERS_VISIBLE = 1000

    private lateinit var mMap: GoogleMap
    private lateinit var mRef: DatabaseReference

    private var mSelectedMarker: Marker? = null
    private val containerMarkerMap: HashMap<String, Container> = HashMap()
    private val mDefaultLocation = LatLng(39.925533, 32.866287)
    private var mCurrentZoom = DEFAULT_ZOOM

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

//        initDb()
        mRef.child("containers").addValueEventListener(containerListener)
    }

//    private fun initDb() {
//        val date = Date()
//
//        for(i in 1..4000) {
//            val container = Container(
//                    i.toString(),
//                    (1..4000).random(),
//                    ((0..5000).random().toDouble() / 1000) + 36.5,
//                    ((0..17000).random().toDouble() / 1000) + 27,
//                    (30..40).random().toDouble(),
//                    (0..100).random(),
//                    date.toString()
//            )
//            mRef.child("/containers/${container.containerId}").setValue(container)
//        }
//    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener {
            val container = containerMarkerMap[it.title]

            mSelectedMarker = it
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Container: ${container?.containerId}")
            builder.setMessage("Sensor ID: ${container?.sensorId}\nTemperature: ${container?.temperature}\nOccupancy Rate: ${container?.rate}%\nLast Read Date: ${container?.date}")
            builder.setPositiveButton("OK", DialogInterface.OnClickListener(okButtonClick))
            builder.setNegativeButton("Relocate", DialogInterface.OnClickListener(relocateButtonClick))
            builder.show()

            true
        }

        mMap.setOnCameraMoveListener {
            val change = abs(mMap.cameraPosition.zoom.toDouble() - mCurrentZoom)
            if(change > ZOOM_RESOLUTION) {
                mCurrentZoom = mMap.cameraPosition.zoom
                loadMarkers()
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM))
    }

    private val relocateButtonClick = { _: DialogInterface, _: Int ->
        val intent = Intent(this, RelocateActivity::class.java)
        intent.putExtra("container", mSelectedMarker?.title)
        startActivityForResult(intent, RELOCATE_REQUEST)
    }

    private val okButtonClick = { _: DialogInterface, _: Int ->
        mSelectedMarker = null
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
                    containerData["rate"].toString().toInt(),
                    containerData["date"].toString())

            containerMarkerMap[container.containerId!!] = container
        }
        loadMarkers()
    }

    private fun loadMarkers() {
        mMap.clear()
        val bounds = mMap.projection.visibleRegion.latLngBounds
        var i = 0
        for((_, container) in containerMarkerMap) {
            if(i < MAX_MARKERS_VISIBLE) {
                if (bounds.contains(LatLng(container.lat!!, container.long!!))) {
                    addToMap(container)
                }
                i++
            } else {
                break
            }
        }
    }

    private fun addToMap(container: Container) {
        val position = LatLng(container.lat!!, container.long!!)

        val iconGenerator = IconGenerator(this)
        iconGenerator.setTextAppearance(R.style.mapMarkerLabelText)

        val marker = mMap.addMarker(MarkerOptions()
                .position(position)
                .title("${container.containerId}")
                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${container.rate}%"))))

        containerMarkerMap[marker.title] = container
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RELOCATE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectedMarker = null
                val newLocation = LatLng(data!!.getDoubleExtra("lat", -34.0),
                        data.getDoubleExtra("long", 151.0))
                val container = containerMarkerMap[data.getStringExtra("container")]
                if(container != null) {
                    addToMap(container)
                    containerMarkerMap[data.getStringExtra("container")]?.lat = newLocation.latitude
                    containerMarkerMap[data.getStringExtra("container")]?.long = newLocation.longitude
                    mRef.child("/containers/${container.containerId}").setValue(container)
                }
            }
        }
    }

}
