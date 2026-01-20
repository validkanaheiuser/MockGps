package moe.yukoDotMoe.cuteanimegirl.ui.screens.viewmodels

import android.location.Address
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import moe.yukoDotMoe.cuteanimegirl.models.LatLng
import moe.yukoDotMoe.cuteanimegirl.extensions.displayString
import moe.yukoDotMoe.cuteanimegirl.service.LocationHelper
import moe.yukoDotMoe.cuteanimegirl.service.MockLocationService
import moe.yukoDotMoe.cuteanimegirl.storage.StorageManager
import moe.yukoDotMoe.cuteanimegirl.ui.models.LocationEntry

class MapViewModel : ViewModel() {
    var markerPosition: MutableState<LatLng> = mutableStateOf(StorageManager.getLatestLocation())
        private set
    var address: MutableState<Address?> = mutableStateOf(null)
        private set

    var markerPositionIsFavorite: MutableState<Boolean> = mutableStateOf(false)

    fun updateMarkerPosition(latLng: LatLng) {
        markerPosition.value = latLng
        MockLocationService.instance?.latLng = latLng

        LocationHelper.reverseGeocoding(latLng) { foundAddress ->
            address.value = foundAddress
        }

        checkIfFavorite()
    }

    fun toggleFavoriteForLocation() {
        StorageManager.toggleFavoriteForPosition(currentLocationEntry())
        checkIfFavorite()
    }

    private fun checkIfFavorite() {
        val currentLocationEntry = currentLocationEntry()
        markerPositionIsFavorite.value = StorageManager.containsFavoriteEntry(currentLocationEntry)
    }

    private fun currentLocationEntry(): LocationEntry {
        return LocationEntry(
            latLng = markerPosition.value,
            addressLine = address.value?.displayString()
        )
    }

}