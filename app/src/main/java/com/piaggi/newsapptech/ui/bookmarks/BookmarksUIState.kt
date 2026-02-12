package com.piaggi.newsapptech.ui.bookmarks

import com.piaggi.newsapptech.domain.entity.Article

data class BookmarksState(
    val articles: List<Article> = emptyList()
)