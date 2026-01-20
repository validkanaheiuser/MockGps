package moe.yukoDotMoe.cuteanimegirl.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import moe.yukoDotMoe.cuteanimegirl.MainActivity
import moe.yukoDotMoe.cuteanimegirl.R
import moe.yukoDotMoe.cuteanimegirl.extensions.roundedShadow
import moe.yukoDotMoe.cuteanimegirl.extensions.toGeoPoint
import moe.yukoDotMoe.cuteanimegirl.extensions.toLatLng
import moe.yukoDotMoe.cuteanimegirl.models.LatLng
import moe.yukoDotMoe.cuteanimegirl.service.LocationHelper
import moe.yukoDotMoe.cuteanimegirl.storage.StorageManager
import moe.yukoDotMoe.cuteanimegirl.ui.components.FavoritesListComponent
import moe.yukoDotMoe.cuteanimegirl.ui.components.FooterComponent
import moe.yukoDotMoe.cuteanimegirl.ui.components.SearchComponent
import moe.yukoDotMoe.cuteanimegirl.ui.screens.viewmodels.MapViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    activity: MainActivity,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isMocking by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val cantSearchWhileMocking = stringResource(R.string.cant_search_while_mocking)
    val cantSwitchWhileMocking = stringResource(R.string.cant_switch_while_mocking)
    val failedGetIpLocation = stringResource(R.string.failed_get_ip_location)
    val selectedLocation = stringResource(R.string.selected_location)

    fun updateMarkerOnMap(latLng: LatLng) {
        mapView?.let { map ->
            marker?.let { map.overlays.remove(it) }

            val newMarker = Marker(map).apply {
                position = latLng.toGeoPoint()
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = selectedLocation
            }
            marker = newMarker
            map.overlays.add(newMarker)
            map.invalidate()
        }
    }

    fun animateCamera(latLng: LatLng) {
        mapView?.controller?.animateTo(latLng.toGeoPoint(), 15.0, 1000L)
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // OSMDroid Map
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(mapViewModel.markerPosition.value.toGeoPoint())

                    // Add initial marker
                    val initialMarker = Marker(this).apply {
                        position = mapViewModel.markerPosition.value.toGeoPoint()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = selectedLocation
                    }
                    overlays.add(initialMarker)
                    marker = initialMarker
                    mapView = this

                    // Request permissions
                    LocationHelper.requestPermissions(activity)
                    mapViewModel.updateMarkerPosition(mapViewModel.markerPosition.value)

                    // Handle map clicks using MapEventsOverlay
                    val mapEventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (!isMocking && p != null) {
                                val latLng = p.toLatLng()
                                mapViewModel.updateMarkerPosition(latLng)
                                updateMarkerOnMap(latLng)
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            return false
                        }
                    }
                    overlays.add(0, MapEventsOverlay(mapEventsReceiver))
                }
            },
            update = { map ->
                mapView = map
            }
        )

        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            SearchComponent(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight(0.075f)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .roundedShadow(32.dp)
                    .zIndex(32f),
                onSearch = { searchTerm ->
                    if (isMocking) {
                        Toast.makeText(
                            activity,
                            cantSearchWhileMocking,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@SearchComponent
                    }

                    LocationHelper.geocoding(searchTerm) { foundLatLng ->
                        foundLatLng?.let {
                            mapViewModel.updateMarkerPosition(it)
                            updateMarkerOnMap(it)
                            animateCamera(it)
                        }
                    }
                }
            )

            // Favorites button
            IconButton(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 12.dp),
                onClick = { showBottomSheet = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Blue, contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.List,
                    contentDescription = stringResource(R.string.show_favorites)
                )
            }
        }

        FooterComponent(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(1f)
                .navigationBarsPadding()
                .padding(4.dp)
                .zIndex(32f)
                .roundedShadow(16.dp),
            address = mapViewModel.address.value,
            latLng = mapViewModel.markerPosition.value,
            isMocking = isMocking,
            isFavorite = mapViewModel.markerPositionIsFavorite.value,
            onStart = { isMocking = activity.toggleMocking() },
            onFavorite = { mapViewModel.toggleFavoriteForLocation() },
            onIpLocation = {
                if (isMocking) {
                    Toast.makeText(
                        activity,
                        cantSwitchWhileMocking,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@FooterComponent
                }
                scope.launch {
                    val ipLocation = LocationHelper.getLocationFromIp()
                    if (ipLocation != null) {
                        mapViewModel.updateMarkerPosition(ipLocation)
                        updateMarkerOnMap(ipLocation)
                        animateCamera(ipLocation)
                    } else {
                        Toast.makeText(
                            activity,
                            failedGetIpLocation,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        if (showBottomSheet) {
            FavoritesListComponent(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                data = StorageManager.favorites,
                onEntryClicked = { clickedEntry ->
                    if (isMocking) {
                        Toast.makeText(
                            activity,
                            cantSwitchWhileMocking,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@FavoritesListComponent
                    }
                    mapViewModel.updateMarkerPosition(clickedEntry.latLng)
                    updateMarkerOnMap(clickedEntry.latLng)
                    scope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                    animateCamera(clickedEntry.latLng)
                }
            )
        }
    }
}
