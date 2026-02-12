package com.piaggi.newsapptech.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.GetTopHeadlinesUseCase
import com.piaggi.newsapptech.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUIState())
    val state = _state.asStateFlow()

    private var currentPage = 1
    private var isLoading = false
    private var allArticles = mutableListOf<Article>()
    private val loadedPages = mutableSetOf<Int>()

    init {
        loadNews(refresh = false)
    }

    fun loadNews(refresh: Boolean = false) {
        if (isLoading && !refresh) return

        if (refresh) {
            currentPage = 1
            allArticles.clear()
            loadedPages.clear()
            _state.value = _state.value.copy(isRefreshing = true)
        } else {
            _state.value = _state.value.copy(isLoading = true)
        }
        isLoading = true

        viewModelScope.launch {
            getTopHeadlinesUseCase(currentPage, refresh).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        if (refresh) {
                            _state.value = _state.value.copy(isRefreshing = true)
                        }
                    }
                    is Resource.Success -> {
                        val newArticles = result.data ?: emptyList()
                        if (refresh) {
                            allArticles.clear()
                            loadedPages.clear()
                        }
                        loadedPages.add(currentPage)
                        newArticles.forEach { article ->
                            val existingIndex = allArticles.indexOfFirst { it.id == article.id }
                            if (existingIndex == -1) {
                                allArticles.add(article)
                            } else {
                                allArticles[existingIndex] = article
                            }
                        }
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            articles = allArticles.toList(),
                            error = null,
                            isRefreshing = false,
                            isOffline = false
                        )
                        isLoading = false
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.message,
                            isRefreshing = false,
                            hasError = true
                        )
                        isLoading = false
                    }
                }
            }
        }
    }

    fun loadMore() {
        if (isLoading || loadedPages.contains(currentPage + 1)) return
        currentPage++
        _state.value = _state.value.copy(isLoadingMore = true)
        loadNews()
    }

    fun refresh() {
        loadNews(refresh = true)
    }
}