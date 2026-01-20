package com.lilstiffy.mockgps.extensions

import com.lilstiffy.mockgps.models.LatLng
import org.osmdroid.util.GeoPoint

fun LatLng.equalTo(other: LatLng): Boolean {
    return (latitude == other.latitude && longitude == other.longitude)
}

fun LatLng.prettyPrint(): String {
    return "Lat: ${this.latitude}\nLng: ${this.longitude}"
}

fun LatLng.toGeoPoint(): GeoPoint {
    return GeoPoint(this.latitude, this.longitude)
}

fun GeoPoint.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}