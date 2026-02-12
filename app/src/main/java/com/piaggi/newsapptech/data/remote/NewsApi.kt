package com.piaggi.newsapptech.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "technology",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5
    ): NewsResponse

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 5
    ): NewsResponse
}