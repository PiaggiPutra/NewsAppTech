package com.piaggi.newsapptech.ui.search

import com.piaggi.newsapptech.ui.model.NewsListItem

data class SearchUIState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val newsListItems: List<NewsListItem> = emptyList(),
    val error: String? = null,
    val query: String = "",
    val hasError: Boolean = false,
    val hasReachedEnd: Boolean = false
)