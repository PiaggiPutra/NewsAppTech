package com.piaggi.newsapptech.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.piaggi.newsapptech.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE id = :articleId)")
    suspend fun isArticleBookmarked(articleId: String): Boolean

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteById(articleId: String)
}