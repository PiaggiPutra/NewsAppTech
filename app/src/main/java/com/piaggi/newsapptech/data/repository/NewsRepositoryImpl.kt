package com.piaggi.newsapptech.data.repository

import com.piaggi.newsapptech.data.local.dao.ArticleDao
import com.piaggi.newsapptech.data.local.dao.CachedHeadlineDao
import com.piaggi.newsapptech.data.local.entity.CachedHeadlineEntity
import com.piaggi.newsapptech.data.local.mapper.toDomainList
import com.piaggi.newsapptech.data.local.mapper.toDomainListFromCached
import com.piaggi.newsapptech.data.remote.NewsApi
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.repository.NewsRepository
import com.piaggi.newsapptech.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApi,
    private val dao: ArticleDao,
    private val cachedHeadlineDao: CachedHeadlineDao
) : NewsRepository {

    override fun getTopHeadlines(page: Int, forceRefresh: Boolean): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getTopHeadlines(page = page)
            val articles = response.articles.toDomainList()

            val articlesWithBookmarkStatus = articles.map { article ->
                val isBookmarked = dao.isArticleBookmarked(article.id)
                article.copy(isBookmarked = isBookmarked)
            }

            val cachedEntities = articlesWithBookmarkStatus.map { article ->
                CachedHeadlineEntity(
                    id = article.id,
                    title = article.title,
                    description = article.description,
                    url = article.url,
                    urlToImage = article.urlToImage,
                    source = article.source,
                    author = article.author,
                    publishedAt = article.publishedAt,
                    content = article.content,
                    page = page
                )
            }
            cachedHeadlineDao.insertHeadlines(cachedEntities)

            emit(Resource.Success(articlesWithBookmarkStatus))
        } catch (e: HttpException) {
            val cachedHeadlinesList = cachedHeadlineDao.getCachedHeadlinesByPage(page).first()
            if (cachedHeadlinesList.isNotEmpty()) {
                val articles = cachedHeadlinesList.toDomainListFromCached()
                val articlesWithBookmarkStatus = articles.map { article ->
                    val isBookmarked = dao.isArticleBookmarked(article.id)
                    article.copy(isBookmarked = isBookmarked)
                }
                emit(Resource.Success(articlesWithBookmarkStatus))
            } else {
                emit(Resource.Error(e.message ?: "An unexpected error occurred"))
            }
        } catch (e: IOException) {
            val cachedHeadlinesList = cachedHeadlineDao.getCachedHeadlinesByPage(page).first()
            if (cachedHeadlinesList.isNotEmpty()) {
                val articles = cachedHeadlinesList.toDomainListFromCached()
                val articlesWithBookmarkStatus = articles.map { article ->
                    val isBookmarked = dao.isArticleBookmarked(article.id)
                    article.copy(isBookmarked = isBookmarked)
                }
                emit(Resource.Success(articlesWithBookmarkStatus))
            } else {
                emit(Resource.Error("Couldn't reach server. Check your internet connection."))
            }
        }
    }

    override fun getCachedHeadlines(): Flow<List<Article>> {
        return cachedHeadlineDao.getAllCachedHeadlines().map { entities ->
            val articles = entities.toDomainListFromCached()
            articles.map { article ->
                val isBookmarked = dao.isArticleBookmarked(article.id)
                article.copy(isBookmarked = isBookmarked)
            }
        }
    }
}