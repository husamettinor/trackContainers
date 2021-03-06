package com.tykesoft.trackcontainers.model

import java.util.*

data class Container (
    var containerId: String? = null,
    var sensorId: Int? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var temperature: Double? = null,
    var rate: Int? = null,
    var date: String? = null
)