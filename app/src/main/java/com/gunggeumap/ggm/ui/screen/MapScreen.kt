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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gunggeumap.ggm.ui.component.*
import com.gunggeumap.ggm.ui.map.addCustomMarker
import com.gunggeumap.ggm.ui.permission.RequestLocationPermission
import com.gunggeumap.ggm.ui.permission.SettingsPermissionDialog
import com.gunggeumap.ggm.ui.viewmodel.dto.MapQuestionDetail
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit = {},
    onQuestionClick: () -> Unit = {}
) {
    /* ───────── 기본 준비 ───────── */
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapView = rememberMapViewWithLifecycle()
    val locationSource = remember { FusedLocationSource(context as Activity, 1000) }
    val naverMapState = remember { mutableStateOf<NaverMap?>(null) }

    /* ───────── 권한/다이얼로그 ───────── */
    var locationGranted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    /* ───────── 검색 상태 ───────── */
    var searchQuery by remember { mutableStateOf("") }
    var lastQueried by remember { mutableStateOf("") }             // 마지막 검색 키워드
    var searchResults by remember { mutableStateOf<List<MapQuestionSummary>>(emptyList()) }
    var showSearchSheet by remember { mutableStateOf(false) }

    /* ───────── 카테고리 선택 ───────── */
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    /* ───────── 지도 데이터 캐싱 ───────── */
    var lastBounds by remember { mutableStateOf<LatLngBounds?>(null) }
    val questionCache = remember { mutableMapOf<String, List<Pair<MapQuestionSummary, Marker>>>() }

    /* ───────── 상세 시트 상태 ───────── */
    var selectedQuestionDetail by remember { mutableStateOf<MapQuestionDetail?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    /* ───────── 위치 권한 요청 ───────── */
    RequestLocationPermission(
        onPermissionGranted = { locationGranted = true },
        onPermissionDenied = { locationGranted = false },
        onPermissionPermanentlyDenied = { showSettingsDialog = true }
    )

    /* ───────── 유틸 함수 ───────── */
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
        val zoom = map.cameraPosition.zoom
        val showTitle = zoom >= 13.5f
        questionCache.values.flatten().forEach { (q, m) ->
            m.captionText = if (showTitle) q.title else ""
        }
    }

    /* ───────── 지도 영역 질문 로딩 ───────── */
    fun loadQuestionsForBounds(map: NaverMap) {
        val bounds = map.contentBounds
        if (!hasMovedSignificantly(lastBounds, bounds)) {
            updateMarkerCaptions(map)
            return
        }
        lastBounds = bounds
        val key = bounds.cacheKey()
        val showTitle = map.cameraPosition.zoom >= 13.5f

        questionCache[key]?.let { cached ->
            cached.forEach { (q, m) -> m.captionText = if (showTitle) q.title else "" }
            return
        }

        scope.launch {
            val res = withContext(Dispatchers.IO) {
                ApiClient.api.getQuestionsInMapBounds(
                    bounds.southWest.latitude, bounds.southWest.longitude,
                    bounds.northEast.latitude, bounds.northEast.longitude
                )
            }
            if (res.success && res.data != null) {
                val markers = res.data.map { q ->
                    val m = addCustomMarker(
                        naverMap = map,
                        position = LatLng(q.latitude, q.longitude),
                        title = if (showTitle) q.title else ""
                    ) {
                        scope.launch {
                            val dRes = ApiClient.api.getQuestionDetail(q.id)
                            if (dRes.success && dRes.data != null) {
                                selectedQuestionDetail = dRes.data
                                showDetailSheet = true
                            }
                        }
                    }
                    q to m
                }
                questionCache[key] = markers
            }
        }
    }

    /* ───────── 검색 실행 ───────── */
    fun performSearch(force: Boolean = false) {
        if (searchQuery.isBlank()) return              // 공백만 막고, 한 글자 허용

        if (!force && searchQuery == lastQueried) {
            showSearchSheet = true                     // 같은 키워드: 시트만
            return
        }
        lastQueried = searchQuery
        scope.launch {
            val res = ApiClient.api.searchQuestionsByKeyword(searchQuery)
            if (res.success && res.data != null) {
                searchResults = res.data
                // 키보드가 이미 Hide 됐으므로 바로 시트 표시
                showSearchSheet = true
            }
        }
    }


    /* ───────── 텍스트 변경 디바운스 ───────── */
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            showSearchSheet = false
            return@LaunchedEffect
        }
        delay(300)
        performSearch(force = false)
    }

    /* ───────── 초기 위치 이동 ───────── */
    LaunchedEffect(locationGranted) {
        if (!locationGranted) return@LaunchedEffect
        val perm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (perm == PackageManager.PERMISSION_GRANTED) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation.addOnSuccessListener { loc ->
                loc?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    naverMapState.value?.let { m ->
                        m.moveCamera(CameraUpdate.scrollTo(latLng))
                        m.locationSource = locationSource
                        locationSource.activate {}
                        m.locationTrackingMode = LocationTrackingMode.Follow
                        loadQuestionsForBounds(m)
                    }
                }
            }
        }
    }

    /* ───────── UI ───────── */
    Box(Modifier.fillMaxSize()) {

        /* 지도 */
        if (locationGranted) {
            AndroidView(factory = { mapView }) {
                mapView.getMapAsync { m ->
                    m.locationSource = locationSource
                    m.locationTrackingMode = LocationTrackingMode.Follow
                    m.addOnCameraIdleListener { loadQuestionsForBounds(m) }
                    m.addOnCameraChangeListener { _, _ -> updateMarkerCaptions(m) }
                    naverMapState.value = m
                }
            }
        }

        /* 상단 검색 & 카테고리 */
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.TopCenter)
        ) {
            Spacer(Modifier.height(16.dp))
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onSearch  = { performSearch(force = true) },        // 검색 아이콘/IME
                placeholder = "궁금한 질문을 검색해 보세요!"
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Category.entries.forEach { cat ->
                    CategoryButton(
                        label = cat.label,
                        selected = cat == selectedCategory,
                        onClick = {
                            selectedCategory = if (cat == selectedCategory) null else cat
                        }
                    )
                }
            }
        }

        /* 플로팅 버튼들 */
        QuestionButton(
            onClick = onQuestionClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
        FloatingActionButton(
            onClick = { naverMapState.value?.locationTrackingMode = LocationTrackingMode.Follow },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 100.dp)
        ) { Icon(Icons.Default.MyLocation, contentDescription = "내 위치") }

        /* ───── 검색 결과 시트 ───── */
        if (showSearchSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showSearchSheet = false },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SearchResultBottomSheet(
                    results = searchResults,
                    onItemClick = { item ->
                        showSearchSheet = false
                        naverMapState.value?.moveCamera(
                            CameraUpdate.scrollTo(LatLng(item.latitude, item.longitude))
                        )
                        scope.launch {
                            val dRes = ApiClient.api.getQuestionDetail(item.id)
                            if (dRes.success && dRes.data != null) {
                                selectedQuestionDetail = dRes.data
                                showDetailSheet = true
                            }
                        }
                    }
                )
            }
        }

        /* ───── 상세 시트 ───── */
        if (showDetailSheet && selectedQuestionDetail != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showDetailSheet = false },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                QuestionBottomSheet(
                    detail = selectedQuestionDetail!!,
                    onNavigateToDetail = { /* TODO */ },
                    onDismiss = { showDetailSheet = false }
                )
            }
        }

        /* 권한 미허용 안내 */
        if (!locationGranted) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
            ) {
                Text(
                    "📍 위치 권한이 필요합니다",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    /* ───────── 권한 설정 다이얼로그 ───────── */
    if (showSettingsDialog) {
        SettingsPermissionDialog(
            onDismiss = { showSettingsDialog = false },
            onGoToSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                showSettingsDialog = false
            }
        )
    }
}

/* ───────────────────────────────────────────── */
/* MapView 라이프사이클 연동 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) = mapView.onCreate(Bundle())
            override fun onStart(owner: LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }
    return mapView
}
