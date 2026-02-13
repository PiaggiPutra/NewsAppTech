package com.piaggi.newsapptech.ui.search

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.usecase.BookmarkUseCase
import com.piaggi.newsapptech.domain.usecase.SearchUseCase
import com.piaggi.newsapptech.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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
class SearchViewModelTest {

    @Mock
    private lateinit var searchUseCase: SearchUseCase

    @Mock
    private lateinit var bookmarkUseCase: BookmarkUseCase

    private lateinit var viewModel: SearchViewModel
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
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        whenever(searchUseCase(any(), any())).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(emptyList()))
            }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        viewModel = SearchViewModel(searchUseCase, bookmarkUseCase)

        val state = viewModel.state.value
        assertEquals("", state.query)
        assertTrue(state.newsListItems.isEmpty())
    }

    @Test
    fun `search should trigger after debounce`() = runTest {
        whenever(searchUseCase.invoke(eq("kotlin"), eq(1))).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(mockArticles))
            }
        )

        viewModel = SearchViewModel(searchUseCase, bookmarkUseCase)

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(500)
        advanceUntilIdle()

        verify(searchUseCase).invoke("kotlin", 1)
        assertEquals(1, viewModel.state.value.newsListItems.size)
    }

    @Test
    fun `empty query should clear results`() = runTest {
        whenever(searchUseCase.invoke(eq("kotlin"), any())).thenReturn(
            flow {
                emit(Resource.Success(mockArticles))
            }
        )

        viewModel = SearchViewModel(searchUseCase, bookmarkUseCase)

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.onQueryChange("")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.newsListItems.isEmpty())
    }

    @Test
    fun `search error should show error message`() = runTest {
        whenever(searchUseCase.invoke(eq("kotlin"), eq(1))).thenReturn(
            flow {
                emit(Resource.Error("Network error"))
            }
        )

        viewModel = SearchViewModel(searchUseCase, bookmarkUseCase)

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.hasError)
        assertEquals("Network error", viewModel.state.value.error)
    }

    @Test
    fun `toggleBookmark should update article state`() = runTest {
        whenever(searchUseCase.invoke(eq("kotlin"), any())).thenReturn(
            flow {
                emit(Resource.Success(mockArticles))
            }
        )

        viewModel = SearchViewModel(searchUseCase, bookmarkUseCase)

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.toggleBookmark(mockArticles[0])
        advanceUntilIdle()

        verify(bookmarkUseCase).bookmarkArticle(mockArticles[0])
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        whenever(searchUseCase.invoke(eq("kotlin"), eq(1))).thenReturn(
            flow {
                emit(Resource.Error("Network error"))
            }
        )

        viewModel = SearchViewModel(searchUseCase, bookmarkUseCase)

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.clearError()

        assertFalse(viewModel.state.value.hasError)
        assertEquals(null, viewModel.state.value.error)
    }
}