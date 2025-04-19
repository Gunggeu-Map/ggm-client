package com.gunggeumap.ggm.ui.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.gunggeumap.ggm.data.remote.ApiClient
import com.gunggeumap.ggm.model.Category
import com.gunggeumap.ggm.ui.component.CategoryButton
import com.gunggeumap.ggm.ui.component.QuestionButton
import com.gunggeumap.ggm.ui.component.SearchBar
import com.gunggeumap.ggm.ui.map.addCustomMarker
import com.gunggeumap.ggm.ui.permission.RequestLocationPermission
import com.gunggeumap.ggm.ui.permission.SettingsPermissionDialog
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionSummary
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun MapScreen(
    onBackClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var locationGranted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val mapView = rememberMapViewWithLifecycle()
    val locationSource = remember { FusedLocationSource(context as Activity, 1000) }
    val naverMapState = remember { mutableStateOf<NaverMap?>(null) }
    var lastBounds by remember { mutableStateOf<LatLngBounds?>(null) }
    val questionCache = remember { mutableMapOf<String, List<Pair<MapQuestionSummary, Marker>>>() }

    RequestLocationPermission(
        onPermissionGranted = { locationGranted = true },
        onPermissionDenied = { locationGranted = false },
        onPermissionPermanentlyDenied = { showSettingsDialog = true }
    )

    fun LatLngBounds.cacheKey(): String =
        "${southWest.latitude}_${southWest.longitude}_${northEast.latitude}_${northEast.longitude}"

    fun hasMovedSignificantly(old: LatLngBounds?, new: LatLngBounds): Boolean {
        if (old == null) return true
        val threshold = 0.005
        val oldCenter = old.center
        val newCenter = new.center
        return abs(oldCenter.latitude - newCenter.latitude) > threshold ||
                abs(oldCenter.longitude - newCenter.longitude) > threshold
    }

    fun updateMarkerCaptions(map: NaverMap) {
        val zoomLevel = map.cameraPosition.zoom
        val showTitle = zoomLevel >= 13.5f
        questionCache.values.flatten().forEach { (question, marker) ->
            marker.captionText = if (showTitle) question.title else ""
        }
    }

    fun loadQuestionsForBounds(map: NaverMap) {
        val bounds = map.contentBounds
        if (!hasMovedSignificantly(lastBounds, bounds)) {
            updateMarkerCaptions(map)
            return
        }
        lastBounds = bounds
        val key = bounds.cacheKey()
        val zoomLevel = map.cameraPosition.zoom
        val showTitle = zoomLevel >= 13.5f

        val cached = questionCache[key]
        if (cached != null) {
            cached.forEach { (question, marker) ->
                marker.captionText = if (showTitle) question.title else ""
            }
            return
        }

        scope.launch {
            val response = withContext(Dispatchers.IO) {
                ApiClient.api.getQuestionsInMapBounds(
                    swLat = bounds.southWest.latitude,
                    swLng = bounds.southWest.longitude,
                    neLat = bounds.northEast.latitude,
                    neLng = bounds.northEast.longitude
                )
            }
            if (response.success && response.data != null) {
                val newMarkers = response.data.map {
                    val marker = addCustomMarker(
                        map,
                        LatLng(it.latitude, it.longitude),
                        if (showTitle) it.title else ""
                    )
                    it to marker
                }
                questionCache[key] = newMarkers
            }
        }
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            val permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permissionState == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        naverMapState.value?.let { map ->
                            map.moveCamera(CameraUpdate.scrollTo(latLng))
                            map.locationSource = locationSource
                            locationSource.activate {}
                            map.locationTrackingMode = LocationTrackingMode.Follow
                            loadQuestionsForBounds(map)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationGranted) {
            AndroidView(factory = { mapView }) {
                mapView.getMapAsync { naverMap ->
                    locationSource.activate {}
                    naverMap.locationSource = locationSource
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
                    naverMap.addOnCameraIdleListener {
                        loadQuestionsForBounds(naverMap)
                    }
                    naverMap.addOnCameraChangeListener { _, _ ->
                        updateMarkerCaptions(naverMap)
                    }
                    naverMapState.value = naverMap
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .align(Alignment.TopCenter)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "궁금한 질문을 검색해 보세요!"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Category.entries.forEach { category ->
                        CategoryButton(
                            label = category.label,
                            selected = category == selectedCategory,
                            onClick = {
                                selectedCategory =
                                    if (selectedCategory == category) null else category
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomEnd)
            ) {
                QuestionButton(onClick = onQuestionClick)
            }

            FloatingActionButton(
                onClick = {
                    naverMapState.value?.locationTrackingMode = LocationTrackingMode.Follow
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 100.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "내 위치")
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
            ) {
                Text(
                    text = "📍 위치 권한이 필요합니다",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showSettingsDialog) {
        SettingsPermissionDialog(
            onDismiss = { showSettingsDialog = false },
            onGoToSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                (context as? Activity)?.startActivity(intent)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) = mapView.onCreate(Bundle())
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}
