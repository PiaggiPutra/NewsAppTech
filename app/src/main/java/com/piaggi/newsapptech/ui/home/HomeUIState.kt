package com.piaggi.newsapptech.ui.home

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.ui.model.NewsListItem

data class HomeUIState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val newsListItems: List<NewsListItem> = emptyList(),
    val error: String? = null,
    val hasError: Boolean = false,
    val isOffline: Boolean = false
)
