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
import com.gunggeumap.ggm.model.Category
import com.gunggeumap.ggm.ui.component.CategoryButton
import com.gunggeumap.ggm.ui.component.QuestionButton
import com.gunggeumap.ggm.ui.component.SearchBar
import com.gunggeumap.ggm.ui.permission.RequestLocationPermission
import com.gunggeumap.ggm.ui.permission.SettingsPermissionDialog
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.google.android.gms.location.LocationServices

@Composable
fun MapScreen(
    onBackClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val mapView = rememberMapViewWithLifecycle()
    val locationSource = remember { FusedLocationSource(context as Activity, 1000) }
    val naverMapState = remember { mutableStateOf<NaverMap?>(null) }

    RequestLocationPermission(
        onPermissionGranted = { locationGranted = true },
        onPermissionDenied = { locationGranted = false },
        onPermissionPermanentlyDenied = { showSettingsDialog = true }
    )

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            val permissionState = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permissionState == PackageManager.PERMISSION_GRANTED) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        naverMapState.value?.moveCamera(CameraUpdate.scrollTo(latLng))
                        naverMapState.value?.locationSource = locationSource
                        locationSource.activate {}
                        naverMapState.value?.locationTrackingMode = LocationTrackingMode.Follow
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationGranted) {
            AndroidView(factory = { mapView }) { view ->
                mapView.getMapAsync { naverMap ->
                    locationSource.activate {}
                    naverMap.locationSource = locationSource
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
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
