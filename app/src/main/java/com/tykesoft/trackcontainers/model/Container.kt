package com.tykesoft.trackcontainers.model

data class Container (
    var containerId: String? = null,
    var sensorId: Int? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var temperature: Double? = null,
    var rate: Int? = null
)