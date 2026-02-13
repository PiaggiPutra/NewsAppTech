package com.piaggi.newsapptech.ui.bookmarks

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {

    @Mock
    private lateinit var bookmarkUseCase: BookmarkUseCase

    private lateinit var viewModel: BookmarksViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockBookmarkedArticles = listOf(
        Article(
            id = "1",
            title = "Bookmarked Article 1",
            description = "Description 1",
            url = "url1",
            urlToImage = "image1",
            publishedAt = "2024-01-01",
            source = "Source 1",
            author = "test",
            content = "testt",
            isBookmarked = true
        ),
        Article(
            id = "2",
            title = "Bookmarked Article 2",
            description = "Description 2",
            url = "url2",
            urlToImage = "image2",
            publishedAt = "2024-01-02",
            source = "Source 2",
            author = "test",
            content = "testt",
            isBookmarked = true
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

    @Test
    fun `init should load bookmarked articles`() = runTest {
        // Given
        whenever(bookmarkUseCase.getBookmarkedArticles()).thenReturn(
            flow { emit(mockBookmarkedArticles) }
        )

        // When
        viewModel = BookmarksViewModel(bookmarkUseCase)
        advanceUntilIdle()

        // Then
        verify(bookmarkUseCase).getBookmarkedArticles()
        assertEquals(mockBookmarkedArticles.size, viewModel.state.value.articles.size)
    }

    @Test
    fun `state should update when bookmarks change`() = runTest {
        // Given
        whenever(bookmarkUseCase.getBookmarkedArticles()).thenReturn(
            flow {
                emit(mockBookmarkedArticles)
                emit(mockBookmarkedArticles.take(1)) // Simulate one removed
            }
        )

        // When
        viewModel = BookmarksViewModel(bookmarkUseCase)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.articles.size)
    }

    @Test
    fun `initial state should be empty`() = runTest {
        // Given
        whenever(bookmarkUseCase.getBookmarkedArticles()).thenReturn(
            flow { emit(emptyList()) }
        )

        // When
        viewModel = BookmarksViewModel(bookmarkUseCase)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.articles.isEmpty())
    }

    @Test
    fun `toggleBookmark should remove bookmark when article is bookmarked`() = runTest {
        // Given
        whenever(bookmarkUseCase.getBookmarkedArticles()).thenReturn(
            flow { emit(mockBookmarkedArticles) }
        )

        viewModel = BookmarksViewModel(bookmarkUseCase)
        advanceUntilIdle()

        val article = mockBookmarkedArticles[0]

        // When
        viewModel.toggleBookmark(article)
        advanceUntilIdle()

        // Then
        verify(bookmarkUseCase).removeBookmark(article.id)
    }

    @Test
    fun `toggleBookmark should add bookmark when article is not bookmarked`() = runTest {
        // Given
        val unbookmarkedArticle = mockBookmarkedArticles[0].copy(isBookmarked = false)

        whenever(bookmarkUseCase.getBookmarkedArticles()).thenReturn(
            flow { emit(emptyList()) }
        )

        viewModel = BookmarksViewModel(bookmarkUseCase)
        advanceUntilIdle()

        // When
        viewModel.toggleBookmark(unbookmarkedArticle)
        advanceUntilIdle()

        // Then
        verify(bookmarkUseCase).bookmarkArticle(unbookmarkedArticle)
    }
}