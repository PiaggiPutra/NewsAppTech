package com.piaggi.newsapptech.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import com.piaggi.newsapptech.domain.usecase.SearchUseCase
import com.piaggi.newsapptech.ui.model.NewsListItem
import com.piaggi.newsapptech.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchUseCase,
    private val bookmarkUseCase: BookmarkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUIState())
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var currentPage = 1
    private val allArticles = mutableListOf<Article>()
    private val loadedPages = mutableSetOf<Int>()
    private var currentQuery = ""
    private var isSearching = false

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotEmpty()) {
                        searchNews(query, isNewSearch = true)
                    } else {
                        clearResults()
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
        _searchQuery.value = query
    }

    private fun clearResults() {
        currentPage = 1
        currentQuery = ""
        allArticles.clear()
        loadedPages.clear()
        isSearching = false
        _state.value = _state.value.copy(
            query = "",
            newsListItems = emptyList(),
            isLoading = false,
            isLoadingMore = false,
            error = null,
            hasError = false
        )
    }

    private fun searchNews(query: String, isNewSearch: Boolean = false) {
        if (isSearching && !isNewSearch) return

        if (isNewSearch || query != currentQuery) {
            currentPage = 1
            currentQuery = query
            allArticles.clear()
            loadedPages.clear()
        }

        isSearching = true
        _state.value = _state.value.copy(
            isLoading = isNewSearch,
            isLoadingMore = !isNewSearch,
            error = null,
            hasError = false,
            newsListItems = if (isNewSearch) emptyList()
            else buildNewsListItems(allArticles.toList(), true)
        )

        viewModelScope.launch {
            searchUseCase(query, currentPage).collect { result ->
                when (result) {
                    is Resource.Loading -> {}

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
                            newsListItems = buildNewsListItems(allArticles.toList(), false),
                            error = null,
                            hasError = false
                        )
                        isSearching = false
                    }

                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            newsListItems = buildNewsListItems(allArticles.toList(), false),
                            error = result.message,
                            hasError = true
                        )
                        isSearching = false
                    }
                }
            }
        }
    }

    fun loadMore() {
        if (isSearching || loadedPages.contains(currentPage + 1) || currentQuery.isEmpty()) return

        currentPage++
        searchNews(currentQuery, isNewSearch = false)
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
                bookmarkUseCase.removeBookmark(article.id)
            } else {
                bookmarkUseCase.bookmarkArticle(article)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null, hasError = false)
    }

    companion object {
        private const val SKELETON_COUNT = 5
    }
}