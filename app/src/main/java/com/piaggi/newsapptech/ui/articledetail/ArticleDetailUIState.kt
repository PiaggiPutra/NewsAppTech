package com.piaggi.newsapptech.ui.articledetail

import com.piaggi.newsapptech.domain.entity.Article

data class ArticleDetailUIState(
    val article: Article? = null,
    val isLoading: Boolean = false
)
