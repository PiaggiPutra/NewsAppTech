package com.piaggi.newsapptech.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.piaggi.newsapptech.data.local.entity.CachedHeadlineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedHeadlineDao {
    @Query("SELECT * FROM cached_headlines ORDER BY page ASC, publishedAt DESC")
    fun getAllCachedHeadlines(): Flow<List<CachedHeadlineEntity>>

    @Query("SELECT * FROM cached_headlines WHERE page = :page ORDER BY publishedAt DESC")
    fun getCachedHeadlinesByPage(page: Int): Flow<List<CachedHeadlineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeadlines(headlines: List<CachedHeadlineEntity>)

    @Query("DELETE FROM cached_headlines")
    suspend fun clearAllHeadlines()

    @Query("DELETE FROM cached_headlines WHERE page = :page")
    suspend fun clearHeadlinesByPage(page: Int)

    @Query("SELECT COUNT(*) FROM cached_headlines")
    suspend fun getCachedHeadlinesCount(): Int
}