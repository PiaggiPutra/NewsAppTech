package com.piaggi.newsapptech.data.repository

import com.piaggi.newsapptech.data.local.dao.ArticleDao
import com.piaggi.newsapptech.data.local.dao.CachedHeadlineDao
import com.piaggi.newsapptech.data.local.mapper.toCachedEntities
import com.piaggi.newsapptech.data.local.mapper.toDomainList
import com.piaggi.newsapptech.data.local.mapper.toDomainListFromCached
import com.piaggi.newsapptech.data.local.mapper.toDomainListFromEntity
import com.piaggi.newsapptech.data.local.mapper.toEntity
import com.piaggi.newsapptech.data.remote.NewsApi
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.repository.NewsRepository
import com.piaggi.newsapptech.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val dao: ArticleDao,
    private val cachedHeadlineDao: CachedHeadlineDao
) : NewsRepository {

    override fun getTopHeadlines(
        page: Int,
        forceRefresh: Boolean
    ): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())

        if (!forceRefresh) {
            val cachedData = cachedHeadlineDao.getCachedHeadlinesByPage(page).first()
            if (cachedData.isNotEmpty()) {
                val articlesWithBookmark = cachedData
                    .toDomainListFromCached()
                    .withBookmarkStatus()
                emit(Resource.Success(articlesWithBookmark))
            }
        }

        try {
            val response = api.getTopHeadlines(page = page)
            val articles = response.articles
                .toDomainList()
                .withBookmarkStatus()

            cachedHeadlineDao.insertHeadlines(articles.toCachedEntities(page))

            emit(Resource.Success(articles))

        } catch (e: HttpException) {
            handleNetworkError(page, e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            handleNetworkError(page, null)
        }
    }

    override fun getCachedHeadlines(): Flow<List<Article>> {
        return cachedHeadlineDao.getAllCachedHeadlines().map { entities ->
            entities.toDomainListFromCached().withBookmarkStatus()
        }
    }

    override fun getBookmarkedArticles(): Flow<List<Article>> {
        return dao.getAllBookmarkedArticles().map { entities ->
            entities.toDomainListFromEntity()
        }
    }

    override suspend fun bookmarkArticle(article: Article) {
        dao.insertArticle(article.toEntity())
    }

    override suspend fun removeBookmark(articleId: String) {
        dao.deleteById(articleId)
    }

    override fun searchNews(query: String, page: Int): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.searchNews(query, page = page)
            val articles = response.articles.toDomainList()

            val articlesWithBookmarkStatus = articles.map { article ->
                val isBookmarked = dao.isArticleBookmarked(article.id)
                article.copy(isBookmarked = isBookmarked)
            }

            emit(Resource.Success(articlesWithBookmarkStatus))
        } catch (e: HttpException) {
            val errorMessage = parseErrorMessage(e.response()?.errorBody()?.string())
                ?: "An error occurred while searching"
            emit(Resource.Error(errorMessage))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }

    private suspend fun List<Article>.withBookmarkStatus(): List<Article> {
        return map { article ->
            article.copy(isBookmarked = dao.isArticleBookmarked(article.id))
        }
    }

    private suspend fun FlowCollector<Resource<List<Article>>>.handleNetworkError(
        page: Int,
        errorBody: String?
    ) {
        val cachedData = cachedHeadlineDao.getCachedHeadlinesByPage(page).first()

        if (cachedData.isNotEmpty()) {
            val articles = cachedData.toDomainListFromCached().withBookmarkStatus()
            emit(Resource.Success(articles))
        } else {
            val errorMessage = parseErrorMessage(errorBody)
                ?: "Couldn't reach server. Check your internet connection."
            emit(Resource.Error(errorMessage))
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        return errorBody?.let {
            try {
                val jsonObject = JSONObject(it)
                jsonObject.optString("message", "An unexpected error occurred")
            } catch (ex: Exception) {
                "An unexpected error occurred"
            }
        }
    }
}