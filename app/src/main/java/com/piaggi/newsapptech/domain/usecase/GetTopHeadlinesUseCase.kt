package com.piaggi.newsapptech.domain.usecase

import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.domain.repository.NewsRepository
import com.piaggi.newsapptech.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(page: Int = 1, forceRefresh: Boolean = false): Flow<Resource<List<Article>>> =
        repository.getTopHeadlines(page, forceRefresh)
}
