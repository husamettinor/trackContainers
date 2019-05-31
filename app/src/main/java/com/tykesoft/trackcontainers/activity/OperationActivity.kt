package com.tykesoft.trackcontainers.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
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

    private val RELOCATE_REQUEST = 1

    private lateinit var mMap: GoogleMap
    private lateinit var mRef: DatabaseReference

    private var mSelectedMarker: Marker? = null
    private val containerMarkerMap: HashMap<String, Container> = HashMap()

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMarkerClickListener {
            val container = containerMarkerMap[it.title]

            mSelectedMarker = it
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Container: ${container?.containerId}")
            builder.setMessage("Sensor ID: ${container?.sensorId}\nTemperature: ${container?.temperature}\nOccupancy Rate: ${container?.rate}")
            builder.setPositiveButton("OK", DialogInterface.OnClickListener(okButtonClick))
            builder.setNegativeButton("Relocate", DialogInterface.OnClickListener(relocateButtonClick))
            builder.show()

            true
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.01, 28.97), 5f))
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

        mMap.clear()
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
                .title("${container.containerId}")
                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("${container.rate}%"))))

        container.marker = marker
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
