package moe.yukoDotMoe.cuteanimegirl.ui.models

import moe.yukoDotMoe.cuteanimegirl.models.LatLng

data class LocationEntry(
    var latLng: LatLng,
    var addressLine: String? = null,
)
