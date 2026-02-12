package com.piaggi.newsapptech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_headlines")
data class CachedHeadlineEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val source: String,
    val author: String?,
    val publishedAt: String,
    val content: String?,
    val page: Int,
    val createdAt: Long = System.currentTimeMillis()
)