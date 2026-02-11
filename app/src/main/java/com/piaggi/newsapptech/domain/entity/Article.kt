package com.piaggi.newsapptech.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val source: String,
    val author: String?,
    val publishedAt: String,
    val content: String?,
    var isBookmarked: Boolean = false
) : Parcelable