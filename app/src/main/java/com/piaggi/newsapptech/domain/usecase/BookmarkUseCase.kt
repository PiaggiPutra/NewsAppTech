package com.piaggi.newsapptech.domain.usecase

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend fun bookmarkArticle(article: Article) = repository.bookmarkArticle(article)

    suspend fun removeBookmark(articleId: String) = repository.removeBookmark(articleId)

    fun getBookmarkedArticles(): Flow<List<Article>> = repository.getBookmarkedArticles()
}
