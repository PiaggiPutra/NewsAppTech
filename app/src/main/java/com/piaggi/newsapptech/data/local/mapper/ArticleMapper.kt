package com.piaggi.newsapptech.data.local.mapper

import com.piaggi.newsapptech.data.local.entity.ArticleEntity
import com.piaggi.newsapptech.data.local.entity.CachedHeadlineEntity
import com.piaggi.newsapptech.data.remote.ArticleDto
import com.piaggi.newsapptech.domain.entity.Article
import java.util.UUID

fun ArticleDto.toDomain(): Article {
    return Article(
        id = UUID.nameUUIDFromBytes(url.toByteArray()).toString(),
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        source = source.name,
        author = author,
        publishedAt = publishedAt,
        content = content
    )
}

fun Article.toEntity(): ArticleEntity {
    return ArticleEntity(
        id = id,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        source = source,
        author = author,
        publishedAt = publishedAt,
        content = content
    )
}

fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        source = source,
        author = author,
        publishedAt = publishedAt,
        content = content,
        isBookmarked = true
    )
}

fun CachedHeadlineEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        source = source,
        author = author,
        publishedAt = publishedAt,
        content = content,
        isBookmarked = false
    )
}

fun List<ArticleDto>.toDomainList(): List<Article> = map { it.toDomain() }
fun List<ArticleEntity>.toDomainListFromEntity(): List<Article> = map { it.toDomain() }
fun List<CachedHeadlineEntity>.toDomainListFromCached(): List<Article> = map { it.toDomain() }