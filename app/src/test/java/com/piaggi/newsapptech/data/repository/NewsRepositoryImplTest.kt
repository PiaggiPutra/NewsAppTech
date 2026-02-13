package com.piaggi.newsapptech.data.repository

import com.piaggi.newsapptech.data.local.dao.ArticleDao
import com.piaggi.newsapptech.data.local.dao.CachedHeadlineDao
import com.piaggi.newsapptech.data.local.entity.CachedHeadlineEntity
import com.piaggi.newsapptech.data.remote.ArticleDto
import com.piaggi.newsapptech.data.remote.NewsApi
import com.piaggi.newsapptech.data.remote.NewsResponse
import com.piaggi.newsapptech.data.remote.SourceDto
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.util.Resource
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryImplTest {

    private lateinit var repository: NewsRepositoryImpl
    private val mockApi: NewsApi = mockk()
    private val mockArticleDao: ArticleDao = mockk()
    private val mockCachedHeadlineDao: CachedHeadlineDao = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = NewsRepositoryImpl(mockApi, mockArticleDao, mockCachedHeadlineDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Top Headlines Tests (4 tests) ==========

    @Test
    fun `getTopHeadlines with forceRefresh fetches from API and caches result`() = runTest {
        // Given
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 2,
            articles = listOf(
                ArticleDto(
                    "Title 1",
                    "Desc",
                    "url1",
                    null,
                    SourceDto(null, "Source"),
                    null,
                    "2024-01-01",
                    null
                ),
                ArticleDto(
                    "Title 2",
                    "Desc",
                    "url2",
                    null,
                    SourceDto(null, "Source"),
                    null,
                    "2024-01-02",
                    null
                )
            )
        )

        coEvery { mockApi.getTopHeadlines(page = 1) } returns newsResponse
        coEvery { mockArticleDao.isArticleBookmarked(any()) } returns false
        coEvery { mockCachedHeadlineDao.insertHeadlines(any()) } just Runs

        // When
        val results = repository.getTopHeadlines(page = 1, forceRefresh = true).toList()

        // Then
        val success = results.last() as Resource.Success
        assertEquals(2, success.data?.size)
        assertEquals("Title 1", success.data?.first()?.title)
        coVerify { mockCachedHeadlineDao.insertHeadlines(any()) }
    }

    @Test
    fun `getTopHeadlines without forceRefresh returns cached data then API data`() = runTest {
        // Given
        val cachedEntities = listOf(
            CachedHeadlineEntity(
                "1",
                "Cached",
                "Desc",
                "url",
                null,
                "Source",
                null,
                "2024-01-01",
                null,
                1
            )
        )
        val newsResponse = NewsResponse(
            "ok", 1, listOf(
                ArticleDto(
                    "Fresh",
                    "Desc",
                    "url",
                    null,
                    SourceDto(null, "Source"),
                    null,
                    "2024-01-02",
                    null
                )
            )
        )

        coEvery { mockCachedHeadlineDao.getCachedHeadlinesByPage(1) } returns flowOf(cachedEntities)
        coEvery { mockArticleDao.isArticleBookmarked(any()) } returns false
        coEvery { mockApi.getTopHeadlines(page = 1) } returns newsResponse
        coEvery { mockCachedHeadlineDao.insertHeadlines(any()) } just Runs

        // When
        val results = repository.getTopHeadlines(page = 1, forceRefresh = false).toList()

        // Then
        assertEquals(3, results.size) // Loading + Cached + Fresh
        assertTrue(results[1] is Resource.Success) // Cached data emitted first
        assertTrue(results[2] is Resource.Success) // Then fresh data
    }

    @Test
    fun `getTopHeadlines with network error falls back to cache`() = runTest {
        // Given
        val cachedEntities = listOf(
            CachedHeadlineEntity(
                "1",
                "Cached",
                "Desc",
                "url",
                null,
                "Source",
                null,
                "2024-01-01",
                null,
                1
            )
        )

        coEvery { mockCachedHeadlineDao.getCachedHeadlinesByPage(1) } returns flowOf(cachedEntities)
        coEvery { mockArticleDao.isArticleBookmarked(any()) } returns false
        coEvery { mockApi.getTopHeadlines(page = 1) } throws IOException("No internet")

        // When
        val results = repository.getTopHeadlines(page = 1, forceRefresh = true).toList()

        // Then
        val success = results.last() as Resource.Success
        assertEquals("Cached", success.data?.first()?.title)
    }

    @Test
    fun `getTopHeadlines with network error and no cache returns error`() = runTest {
        // Given
        coEvery { mockCachedHeadlineDao.getCachedHeadlinesByPage(1) } returns flowOf(emptyList())
        coEvery { mockApi.getTopHeadlines(page = 1) } throws IOException("No internet")

        // When
        val results = repository.getTopHeadlines(page = 1, forceRefresh = true).toList()

        // Then
        val error = results.last() as Resource.Error
        assertEquals("Couldn't reach server. Check your internet connection.", error.message)
    }

    // ========== Search Tests (2 tests) ==========

    @Test
    fun `searchNews returns articles from API with bookmark status`() = runTest {
        // Given
        val newsResponse = NewsResponse("ok", 1, listOf(
            ArticleDto(
                "Search Result",
                "Desc",
                "url",
                null,
                SourceDto(null, "Source"),
                null,
                "2024-01-01",
                null
            )
        ))

        coEvery { mockApi.searchNews("kotlin", page = 1) } returns newsResponse
        coEvery { mockArticleDao.isArticleBookmarked(any()) } returns true

        // When
        val results = repository.searchNews("kotlin", page = 1).toList()

        // Then
        val success = results.last() as Resource.Success
        assertEquals("Search Result", success.data?.first()?.title)
        assertTrue(success.data?.first()?.isBookmarked == true)
    }

    @Test
    fun `searchNews with network error returns error message`() = runTest {
        // Given
        coEvery { mockApi.searchNews("test", page = 1) } throws IOException("Connection failed")

        // When
        val results = repository.searchNews("test", page = 1).toList()

        // Then
        val error = results.last() as Resource.Error
        assertEquals("Couldn't reach server. Check your internet connection.", error.message)
    }

    // ========== Bookmark Tests (2 tests) ==========

    @Test
    fun `bookmarkArticle inserts article into database`() = runTest {
        // Given
        val article = Article(
                "1",
                "Test",
                "Desc",
                "url",
                null,
                "Source",
                null,
                "2024-01-01",
                null,
                false
            )
        coEvery { mockArticleDao.insertArticle(any()) } just Runs

        // When
        repository.bookmarkArticle(article)

        // Then
        coVerify { mockArticleDao.insertArticle(match { it.id == "1" && it.title == "Test" }) }
    }

    @Test
    fun `removeBookmark deletes article by id`() = runTest {
        // Given
        coEvery { mockArticleDao.deleteById("article-1") } just Runs

        // When
        repository.removeBookmark("article-1")

        // Then
        coVerify { mockArticleDao.deleteById("article-1") }
    }
}
