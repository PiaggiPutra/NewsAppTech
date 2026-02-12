package com.piaggi.newsapptech.ui.articledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val bookmarkUseCase: BookmarkUseCase
) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(ArticleDetailUIState())
    val state = _state.asStateFlow()

    fun loadArticle(article: Article) {
        _state.value = _state.value.copy(
            article = article,
            isLoading = false
        )
    }

    fun toggleBookmark() {
        state.value.article?.let { article ->
            viewModelScope.launch {
                val updatedArticle = article.copy(isBookmarked = !article.isBookmarked)

                _state.value = _state.value.copy(
                    article = updatedArticle
                )

                if (article.isBookmarked) {
                    bookmarkUseCase.removeBookmark(article.id)
                } else {
                    bookmarkUseCase.bookmarkArticle(article)
                }
            }
        }
    }

    fun shareArticle(context: android.content.Context) {
        state.value.article?.let { article ->
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, article.url)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Article"))
        }
    }
}