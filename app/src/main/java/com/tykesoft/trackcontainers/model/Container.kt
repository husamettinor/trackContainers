package com.tykesoft.trackcontainers.model

import com.google.android.gms.maps.model.Marker

data class Container (
    var containerId: String? = null,
    var sensorId: Int? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var temperature: Double? = null,
    var rate: Int? = null,
    var marker: Marker? = null
)