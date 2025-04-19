package com.gunggeumap.ggm.ui.map

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.gunggeumap.ggm.R

fun addCustomMarker(
    naverMap: NaverMap,
    position: LatLng,
    title: String = "",
    onClick: (() -> Unit)? = null
): Marker {
    return Marker().apply {
        this.position = position
        this.icon = OverlayImage.fromResource(R.drawable.ic_custom_marker)
        this.width = 96
        this.height = 96
        this.captionText = title
        this.captionRequestedWidth = 140
        this.captionColor = 0xFF222222.toInt()
        this.captionHaloColor = 0x00FFFFFF
        this.map = naverMap

        onClick?.let { callback ->
            setOnClickListener {
                callback()
                true
            }
        }
    }
}