package com.piaggi.newsapptech.domain.repository

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.util.Resource
import kotlinx.coroutines.flow.Flow


interface NewsRepository {
    fun getTopHeadlines(page: Int, forceRefresh: Boolean): Flow<Resource<List<Article>>>
    fun getCachedHeadlines(): Flow<List<Article>>

    fun getBookmarkedArticles(): Flow<List<Article>>
    suspend fun bookmarkArticle(article: Article)
    suspend fun removeBookmark(articleId: String)
}