package com.piaggi.newsapptech.ui.home

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import com.piaggi.newsapptech.domain.usecase.GetTopHeadlinesUseCase
import com.piaggi.newsapptech.ui.model.NewsListItem
import com.piaggi.newsapptech.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Mock
    private lateinit var getTopHeadlinesUseCase: GetTopHeadlinesUseCase

    @Mock
    private lateinit var bookmarksUseCase: BookmarkUseCase

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockArticles = listOf(
        Article(
            id = "1",
            title = "Article 1",
            description = "Description 1",
            url = "url1",
            urlToImage = "image1",
            publishedAt = "2024-01-01",
            source = "Source 1",
            author = "test",
            content = "testt",
            isBookmarked = false
        ),
        Article(
            id = "2",
            title = "Article 2",
            description = "Description 2",
            url = "url2",
            urlToImage = "image2",
            publishedAt = "2024-01-02",
            source = "Source 2",
            author = "test",
            content = "testt",
            isBookmarked = false
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultMocks() {
        whenever(getTopHeadlinesUseCase(any(), any())).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )
    }

    @Test
    fun `init should load news on creation`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        // When
        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(getTopHeadlinesUseCase).invoke(1, false)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(mockArticles.size, viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.ArticleItem>().size)
    }

    @Test
    fun `loadNews success should update state with articles`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        // When
        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertFalse(state.hasError)
        assertEquals(mockArticles.size, state.newsListItems.filterIsInstance<NewsListItem.ArticleItem>().size)
    }

    @Test
    fun `loadNews error should update state with error message`() = runTest {
        // Given
        setupDefaultMocks()
        val errorMessage = "Network error"
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Error(errorMessage))
            }
        )

        // When
        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.hasError)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `loadNews with empty result should set hasReachedEnd to true`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )

        // When
        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.hasReachedEnd)
    }

    @Test
    fun `loadMore should increment page and load more articles`() = runTest {
        // Given
        setupDefaultMocks()

        val moreArticles = listOf(
            Article(
                id = "3",
                title = "Article 3",
                description = "Description 3",
                url = "url3",
                urlToImage = "image3",
                publishedAt = "2024-01-03",
                source = "Source 3",
                author = "test",
                content = "testt",
                isBookmarked = false
            )
        )

        // Mock untuk page 1 (initial load)
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        // Mock untuk page 2 (automatic load more)
        whenever(getTopHeadlinesUseCase.invoke(eq(2), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(moreArticles))
            }
        )

        // Mock untuk page 3 (manual load more)
        whenever(getTopHeadlinesUseCase.invoke(eq(3), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialArticleCount = viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.ArticleItem>().size

        // When
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val finalArticleCount = viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.ArticleItem>().size
        assertTrue(finalArticleCount >= initialArticleCount)
    }

    @Test
    fun `loadMore should not load if already loading`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow { emit(Resource.Loading()) }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)

        // When
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(getTopHeadlinesUseCase).invoke(1, false)
    }

    @Test
    fun `loadMore should not load if hasReachedEnd is true`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(getTopHeadlinesUseCase).invoke(1, false)
    }

    @Test
    fun `refresh should reset page and reload articles`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), any())).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(getTopHeadlinesUseCase).invoke(1, true)
    }

    @Test
    fun `toggleBookmark should bookmark article when not bookmarked`() = runTest {
        // Given
        setupDefaultMocks()
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val article = mockArticles[0]

        // When
        viewModel.toggleBookmark(article)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(bookmarksUseCase).bookmarkArticle(article)
        val updatedArticle = viewModel.state.value.newsListItems
            .filterIsInstance<NewsListItem.ArticleItem>()
            .first { it.article.id == article.id }
        assertTrue(updatedArticle.article.isBookmarked)
    }

    @Test
    fun `toggleBookmark should remove bookmark when already bookmarked`() = runTest {
        // Given
        setupDefaultMocks()
        val bookmarkedArticles = listOf(mockArticles[0].copy(isBookmarked = true))

        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(bookmarkedArticles))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val article = bookmarkedArticles[0]

        // When
        viewModel.toggleBookmark(article)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(bookmarksUseCase).removeBookmark(article.id)
        val updatedArticle = viewModel.state.value.newsListItems
            .filterIsInstance<NewsListItem.ArticleItem>()
            .first { it.article.id == article.id }
        assertFalse(updatedArticle.article.isBookmarked)
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given
        setupDefaultMocks()
        val errorMessage = "Network error"
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Error(errorMessage))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertEquals(null, viewModel.state.value.error)
        assertFalse(viewModel.state.value.hasError)
    }

    @Test
    fun `should not add duplicate articles when loading more`() = runTest {
        // Given
        setupDefaultMocks()

        // Page 1
        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        // Page 2 - return same articles (simulating duplicate)
        whenever(getTopHeadlinesUseCase.invoke(eq(2), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        // Page 3
        whenever(getTopHeadlinesUseCase.invoke(eq(3), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val articleItems = viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.ArticleItem>()
        assertEquals(mockArticles.size, articleItems.size)
    }

    @Test
    fun `loadMore should show skeleton items while loading`() = runTest {
        // Given
        setupDefaultMocks()

        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        whenever(getTopHeadlinesUseCase.invoke(eq(2), eq(false))).thenReturn(
            flow { emit(Resource.Loading()) }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadMore()

        // Then
        val skeletonItems = viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.SkeletonItem>()
        assertEquals(3, skeletonItems.size)
    }

    @Test
    fun `should update existing article when same id appears`() = runTest {
        // Given
        setupDefaultMocks()

        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        val updatedArticle = mockArticles[0].copy(
            title = "Updated Title",
            isBookmarked = true
        )

        whenever(getTopHeadlinesUseCase.invoke(eq(2), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(listOf(updatedArticle)))
            }
        )

        whenever(getTopHeadlinesUseCase.invoke(eq(3), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )

        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val articleItems = viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.ArticleItem>()
        val article = articleItems.first { it.article.id == "1" }
        assertEquals("Updated Title", article.article.title)
        assertTrue(article.article.isBookmarked)
    }

    @Test
    fun `loadNews should preserve articles on error after successful load`() = runTest {
        // Given
        setupDefaultMocks()

        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        whenever(getTopHeadlinesUseCase.invoke(eq(2), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Error("Network error"))
            }
        )

        // When
        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - check state after automatic loadMore fails on page 2
        val articleItems = viewModel.state.value.newsListItems.filterIsInstance<NewsListItem.ArticleItem>()
        assertEquals(mockArticles.size, articleItems.size)
        assertTrue(viewModel.state.value.hasError)
    }

    @Test
    fun `automatic loadMore after first page success when not reached end`() = runTest {
        // Given
        setupDefaultMocks()

        val moreArticles = listOf(
            Article(
                id = "3",
                title = "Article 3",
                description = "Description 3",
                url = "url3",
                urlToImage = "image3",
                publishedAt = "2024-01-03",
                source = "Source 3",
                author = "test",
                content = "testt",
                isBookmarked = false
            )
        )

        whenever(getTopHeadlinesUseCase.invoke(eq(1), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        whenever(getTopHeadlinesUseCase.invoke(eq(2), eq(false))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(moreArticles))
            }
        )

        // When
        viewModel = HomeViewModel(getTopHeadlinesUseCase, bookmarksUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should automatically load page 2 after page 1
        verify(getTopHeadlinesUseCase).invoke(1, false)
        verify(getTopHeadlinesUseCase).invoke(2, false)
    }
}