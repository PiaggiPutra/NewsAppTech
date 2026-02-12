package com.piaggi.newsapptech.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val useCase: BookmarkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksState())
    val state = _state.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            useCase.getBookmarkedArticles().collectLatest { articles ->
                _state.value = _state.value.copy(articles = articles)
            }
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            if (article.isBookmarked) {
                useCase.removeBookmark(article.id)
            } else {
                useCase.bookmarkArticle(article)
            }
        }
    }
}