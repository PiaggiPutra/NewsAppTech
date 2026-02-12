package com.piaggi.newsapptech.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import com.piaggi.newsapptech.domain.usecase.GetTopHeadlinesUseCase
import com.piaggi.newsapptech.ui.model.NewsListItem
import com.piaggi.newsapptech.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopHeadlinesUseCase: GetTopHeadlinesUseCase,
    private val bookmarksUseCase: BookmarkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUIState())
    val state = _state.asStateFlow()

    private var currentPage = 1
    private var isLoading = false
    private val allArticles = mutableListOf<Article>()
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
        }

        _state.value = _state.value.copy(
            isLoading = !refresh,
            isRefreshing = refresh,
            error = null,
            hasError = false
        )
        isLoading = true

        viewModelScope.launch {
            getTopHeadlinesUseCase(currentPage, refresh).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        val newArticles = result.data ?: emptyList()

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
                            isRefreshing = false,
                            newsListItems = buildNewsListItems(allArticles.toList(), false),
                            error = null,
                            hasError = false,
                            isOffline = false
                        )
                        isLoading = false
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            newsListItems = buildNewsListItems(allArticles.toList(), false),
                            error = result.message,
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
        _state.value = _state.value.copy(
            isLoadingMore = true,
            newsListItems = buildNewsListItems(allArticles.toList(), true)
        )
        loadNews()
    }

    fun refresh() {
        loadNews(refresh = true)
    }

    private fun buildNewsListItems(
        articles: List<Article>,
        isLoadingMore: Boolean
    ): List<NewsListItem> {
        return articles.map { NewsListItem.ArticleItem(it) } +
                if (isLoadingMore) List(SKELETON_COUNT) { NewsListItem.SkeletonItem }
                else emptyList()
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            val index = allArticles.indexOfFirst { it.id == article.id }
            if (index != -1) {
                val updatedArticle = article.copy(isBookmarked = !article.isBookmarked)
                allArticles[index] = updatedArticle

                _state.value = _state.value.copy(
                    newsListItems = buildNewsListItems(allArticles.toList(), _state.value.isLoadingMore)
                )
            }

            if (article.isBookmarked) {
                bookmarksUseCase.removeBookmark(article.id)
            } else {
                bookmarksUseCase.bookmarkArticle(article)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null, hasError = false)
    }

    companion object {
        private const val SKELETON_COUNT = 3
    }
}