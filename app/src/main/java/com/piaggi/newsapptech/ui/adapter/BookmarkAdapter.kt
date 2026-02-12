package com.piaggi.newsapptech.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piaggi.newsapptech.R
import com.piaggi.newsapptech.databinding.ItemArticleBinding
import com.piaggi.newsapptech.domain.entity.Article

class BookmarkAdapter(
    private val onArticleClick: (Article) -> Unit,
    private val onBookmarkClick: (Article) -> Unit
) : ListAdapter<Article, BookmarkAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                tvTitle.text = article.title
                tvSource.text = article.source
                tvDescription.text = article.description ?: ""

                Glide.with(ivArticleImage)
                    .load(article.urlToImage)
                    .into(ivArticleImage)

                ibBookmark.setImageResource(
                    if (article.isBookmarked) R.drawable.ic_bookmarks
                    else R.drawable.ic_bookmarks_outline
                )

                // Root view click listener for article detail
                root.setOnClickListener {
                    onArticleClick(article)
                }

                // Separate click listener for bookmark
                ibBookmark.setOnClickListener {
                    onBookmarkClick(article)
                }
            }
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}