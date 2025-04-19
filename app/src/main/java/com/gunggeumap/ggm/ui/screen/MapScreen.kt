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
import com.gunggeumap.ggm.data.model.Category
import com.gunggeumap.ggm.data.remote.ApiClient
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
    /* ───── 기본 준비 ───── */
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val mapView = rememberMapViewWithLifecycle()
    val locationSource = remember { FusedLocationSource(context as Activity, 1000) }
    val naverMapState  = remember { mutableStateOf<NaverMap?>(null) }

    /* ───── 권한 상태 ───── */
    var locationGranted     by remember { mutableStateOf(false) }
    var showSettingsDialog  by remember { mutableStateOf(false) }

    /* ───── 검색/카테고리 상태 ───── */
    var searchQuery         by remember { mutableStateOf("") }
    var lastQueried         by remember { mutableStateOf("") }
    var selectedCategory    by remember { mutableStateOf<Category?>(null) }
    var searchResults       by remember { mutableStateOf<List<MapQuestionSummary>>(emptyList()) }
    var showResultSheet     by remember { mutableStateOf(false) }

    /* ───── 상세 시트 상태 ───── */
    var selectedQuestionDetail by remember { mutableStateOf<MapQuestionDetail?>(null) }
    var showDetailSheet        by remember { mutableStateOf(false) }

    /* ───── 지도 마커 캐시 ───── */
    var lastBounds by remember { mutableStateOf<LatLngBounds?>(null) }
    val questionCache = remember { mutableMapOf<String, List<Pair<MapQuestionSummary, Marker>>>() }

    /* ───── 위치 권한 요청 ───── */
    RequestLocationPermission(
        onPermissionGranted         = { locationGranted = true },
        onPermissionDenied          = { locationGranted = false },
        onPermissionPermanentlyDenied = { showSettingsDialog = true }
    )

    /* ───── 유틸 함수 ───── */
    fun LatLngBounds.cacheKey() =
        "${southWest.latitude}_${southWest.longitude}_${northEast.latitude}_${northEast.longitude}"

    fun hasMovedSignificantly(o: LatLngBounds?, n: LatLngBounds): Boolean {
        if (o == null) return true
        val th = 0.005
        return abs(o.center.latitude - n.center.latitude)  > th ||
                abs(o.center.longitude - n.center.longitude) > th
    }

    fun updateCaptions(map: NaverMap) {
        val show = map.cameraPosition.zoom >= 13.5f
        questionCache.values.flatten().forEach { (q, m) ->
            m.captionText = if (show) q.title else ""
        }
    }

    /* ───── 지도 질문 로드 ───── */
    fun loadQuestions(map: NaverMap) {
        val b = map.contentBounds
        if (!hasMovedSignificantly(lastBounds, b)) { updateCaptions(map); return }
        lastBounds = b
        val key = b.cacheKey()
        val showTitle = map.cameraPosition.zoom >= 13.5f

        questionCache[key]?.let { cached ->
            cached.forEach { (q,m) -> m.captionText = if (showTitle) q.title else "" }; return
        }

        scope.launch {
            val res = withContext(Dispatchers.IO) {
                ApiClient.api.getQuestionsInMapBounds(
                    b.southWest.latitude, b.southWest.longitude,
                    b.northEast.latitude, b.northEast.longitude)
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

    /* ───── 키워드 검색 ───── */
    fun keywordSearch(force: Boolean = false) {
        if (!locationGranted) return                  // 권한 없으면 무시
        if (searchQuery.isBlank()) return
        if (!force && searchQuery == lastQueried) { selectedCategory=null; showResultSheet=true; return }
        lastQueried = searchQuery; selectedCategory = null
        scope.launch {
            val res = ApiClient.api.searchQuestionsByKeyword(searchQuery)
            if (res.success && res.data != null) {
                searchResults = res.data; showResultSheet = true
            }
        }
    }

    /* ───── 카테고리 검색 ───── */
    fun categorySearch(cat: Category) {
        if (!locationGranted) return
        scope.launch {
            val res = ApiClient.api.searchQuestionsByCategory(cat.name)
            if (res.success && res.data != null) {
                searchResults = res.data; showResultSheet = true
            }
        }
    }

    /* ───── 검색 디바운스 ───── */
    LaunchedEffect(searchQuery, locationGranted) {
        if (!locationGranted) return@LaunchedEffect
        if (searchQuery.isBlank()) { showResultSheet=false; return@LaunchedEffect }
        delay(300); keywordSearch(false)
    }

    /* ───── 초기 위치 이동 ───── */
    LaunchedEffect(locationGranted) {
        if (!locationGranted) return@LaunchedEffect
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        val here = LatLng(it.latitude, it.longitude)
                        naverMapState.value?.let { m ->
                            m.moveCamera(CameraUpdate.scrollTo(here))
                            m.locationSource = locationSource; locationSource.activate {}
                            m.locationTrackingMode = LocationTrackingMode.Follow
                            loadQuestions(m)
                        }
                    }
                }
        }
    }

    /* ───── UI ───── */
    Box(Modifier.fillMaxSize()) {

        /* 지도 */
        if (locationGranted) {
            AndroidView(factory = { mapView }) {
                mapView.getMapAsync { m ->
                    m.locationSource = locationSource
                    m.locationTrackingMode = LocationTrackingMode.Follow
                    m.addOnCameraIdleListener   { loadQuestions(m) }
                    m.addOnCameraChangeListener { _, _ -> updateCaptions(m) }
                    naverMapState.value = m
                }
            }
        }

        /* ───── 상단 검색 & 카테고리 (권한 있을 때만) ───── */
        if (locationGranted) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .align(Alignment.TopCenter)
            ) {
                Spacer(Modifier.height(16.dp))
                SearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    onSearch = { keywordSearch(true) },
                    placeholder = "궁금한 질문을 검색해 보세요!"
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    Category.entries.forEach { cat ->
                        CategoryButton(
                            label = cat.label,
                            selected = cat == selectedCategory,
                            onClick = {
                                selectedCategory =
                                    if (cat == selectedCategory) null else cat
                                selectedCategory?.let { categorySearch(it) }
                            }
                        )
                    }
                }
            }
        }

        /* 질문 작성 버튼 */
        QuestionButton(
            onClick = {
                if (locationGranted) onQuestionClick() else showSettingsDialog = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        )

        /* 내 위치 버튼 */
        FloatingActionButton(
            onClick = { naverMapState.value?.locationTrackingMode = LocationTrackingMode.Follow },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 100.dp)
        ) { Icon(Icons.Default.MyLocation, contentDescription = "내 위치") }

        /* ───── 검색/카테고리 결과 시트 ───── */
        if (showResultSheet) {
            val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                sheetState = sheet,
                onDismissRequest = { showResultSheet=false; selectedCategory=null },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SearchResultBottomSheet(
                    results = searchResults,
                    onItemClick = { item ->
                        showResultSheet=false; selectedCategory=null
                        naverMapState.value?.moveCamera(
                            CameraUpdate.scrollTo(LatLng(item.latitude, item.longitude))
                        )
                        scope.launch {
                            val dRes = ApiClient.api.getQuestionDetail(item.id)
                            if (dRes.success && dRes.data != null) {
                                selectedQuestionDetail=dRes.data; showDetailSheet=true
                            }
                        }
                    }
                )
            }
        }

        /* ───── 상세 시트 ───── */
        if (showDetailSheet && selectedQuestionDetail != null) {
            val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                sheetState = sheet,
                onDismissRequest = { showDetailSheet=false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                QuestionBottomSheet(
                    detail = selectedQuestionDetail!!,
                    onNavigateToDetail = {},
                    onDismiss = { showDetailSheet=false }
                )
            }
        }

        /* 권한 필요 안내 (중앙) */
        if (!locationGranted) {
            Box(
                Modifier.align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
            ) {
                Text("📍 위치 권한이 필요합니다",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }

    /* ───── 설정 다이얼로그 ───── */
    if (showSettingsDialog) {
        SettingsPermissionDialog(
            onDismiss = { showSettingsDialog=false },
            onGoToSettings = {
                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(i); showSettingsDialog=false
            }
        )
    }
}

/* ───────────────────────────────────────────── */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context   = LocalContext.current
    val mapView   = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = object : DefaultLifecycleObserver {
            override fun onCreate(o: LifecycleOwner) = mapView.onCreate(Bundle())
            override fun onStart (o: LifecycleOwner) = mapView.onStart()
            override fun onResume(o: LifecycleOwner) = mapView.onResume()
            override fun onPause (o: LifecycleOwner) = mapView.onPause()
            override fun onStop  (o: LifecycleOwner) = mapView.onStop()
            override fun onDestroy(o: LifecycleOwner)= mapView.onDestroy()
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }
    return mapView
}
