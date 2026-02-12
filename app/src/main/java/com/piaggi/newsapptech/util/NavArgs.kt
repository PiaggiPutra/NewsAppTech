package com.piaggi.newsapptech.util

import android.os.Bundle
import androidx.navigation.NavController
import com.piaggi.newsapptech.domain.entity.Article

object NavArgs {
    private const val ARTICLE = "article"
    fun getArticle(bundle: Bundle?): Article? {
        return bundle?.getParcelable(ARTICLE)
    }

    fun navigateToArticleDetail(navController: NavController, article: Article, rootViewId: Int) {
        val bundle = Bundle().apply {
            putParcelable(ARTICLE, article)
        }
        navController.navigate(rootViewId, bundle)
    }
}