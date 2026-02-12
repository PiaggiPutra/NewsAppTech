package com.piaggi.newsapptech.ui.home

import com.piaggi.newsapptech.domain.entity.Article

data class HomeUIState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null,
    val hasError: Boolean = false,
    val isOffline: Boolean = false
)
