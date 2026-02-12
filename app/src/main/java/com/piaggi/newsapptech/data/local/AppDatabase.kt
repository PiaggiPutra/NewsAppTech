package com.piaggi.newsapptech.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.piaggi.newsapptech.data.local.dao.ArticleDao
import com.piaggi.newsapptech.data.local.dao.CachedHeadlineDao
import com.piaggi.newsapptech.data.local.entity.ArticleEntity
import com.piaggi.newsapptech.data.local.entity.CachedHeadlineEntity

@Database(
    entities = [ArticleEntity::class, CachedHeadlineEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun cachedHeadlineDao(): CachedHeadlineDao
}