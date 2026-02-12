package com.piaggi.newsapptech.di

import android.content.Context
import androidx.room.Room
import com.piaggi.newsapptech.data.local.AppDatabase
import com.piaggi.newsapptech.data.local.dao.ArticleDao
import com.piaggi.newsapptech.data.local.dao.CachedHeadlineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "news_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    @Singleton
    fun provideCachedHeadlineDao(database: AppDatabase): CachedHeadlineDao {
        return database.cachedHeadlineDao()
    }
}