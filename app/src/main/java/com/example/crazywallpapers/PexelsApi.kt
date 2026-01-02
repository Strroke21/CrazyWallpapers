package com.example.crazywallpapers

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PexelsApi {

    @Headers(
        "Authorization: b1sH3TVLjR2Ba4hHqaObjlrkgyK0yYJG6dQj1bMV7VwMHn6aDTewdzrC"
    )
    @GET("v1/curated")
    suspend fun getWallpapers(
        @Query("per_page") perPage: Int = 50
    ): PexelsResponse

    @Headers(
        "Authorization: b1sH3TVLjR2Ba4hHqaObjlrkgyK0yYJG6dQj1bMV7VwMHn6aDTewdzrC"
    )
    @GET("v1/search")
    suspend fun searchWallpapers(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 50
    ): PexelsResponse
}
