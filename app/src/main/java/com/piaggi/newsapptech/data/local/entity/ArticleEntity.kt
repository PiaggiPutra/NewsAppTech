package com.piaggi.newsapptech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val source: String,
    val author: String?,
    val publishedAt: String,
    val content: String?
)