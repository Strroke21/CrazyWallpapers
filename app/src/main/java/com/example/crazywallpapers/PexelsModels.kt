package com.example.crazywallpapers

data class PexelsResponse(
    val photos: List<PexelsPhoto>
)

data class PexelsPhoto(
    val id: Int,
    val width: Int,
    val height: Int,
    val src: PexelsSrc
)

data class PexelsSrc(
    val medium: String,
    val original: String
)
