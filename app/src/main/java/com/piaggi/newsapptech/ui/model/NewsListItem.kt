package com.piaggi.newsapptech.ui.model

import com.piaggi.newsapptech.domain.entity.Article

sealed class NewsListItem {
    data class ArticleItem(val article: Article) : NewsListItem()

    data object SkeletonItem : NewsListItem()
}
