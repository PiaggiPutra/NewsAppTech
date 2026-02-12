package com.piaggi.newsapptech.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.piaggi.newsapptech.R
import com.piaggi.newsapptech.databinding.ItemArticleBinding
import com.piaggi.newsapptech.databinding.ItemArticleSkeletonBinding
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.ui.model.NewsListItem

class HeadlineAdapter(
    private val onArticleClick: (Article) -> Unit,
    private val onBookmarkClick: (Article) -> Unit
) : ListAdapter<NewsListItem, RecyclerView.ViewHolder>(NewsListItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ARTICLE = 0
        private const val VIEW_TYPE_SKELETON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NewsListItem.ArticleItem -> VIEW_TYPE_ARTICLE
            is NewsListItem.SkeletonItem -> VIEW_TYPE_SKELETON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ARTICLE -> {
                val binding = ItemArticleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ArticleViewHolder(binding)
            }
            VIEW_TYPE_SKELETON -> {
                val binding = ItemArticleSkeletonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SkeletonViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArticleViewHolder -> {
                val item = getItem(position) as NewsListItem.ArticleItem
                holder.bind(item.article)
            }
            is SkeletonViewHolder -> {
                // No binding needed for skeleton
            }
        }
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.apply {
                tvTitle.text = article.title
                tvSource.text = article.source
                tvDescription.text = article.description.orEmpty()

                Glide.with(ivArticleImage)
                    .load(article.urlToImage)
                    .into(ivArticleImage)

                ibBookmark.setImageResource(
                    if (article.isBookmarked) R.drawable.ic_bookmarks
                    else R.drawable.ic_bookmarks_outline
                )

                root.setOnClickListener {
                    onArticleClick(article)
                }

                ibBookmark.setOnClickListener {
                    onBookmarkClick(article)
                }
            }
        }
    }

    inner class SkeletonViewHolder(
        binding: ItemArticleSkeletonBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class NewsListItemDiffCallback : DiffUtil.ItemCallback<NewsListItem>() {
        override fun areItemsTheSame(oldItem: NewsListItem, newItem: NewsListItem): Boolean {
            return when {
                oldItem is NewsListItem.ArticleItem && newItem is NewsListItem.ArticleItem -> {
                    oldItem.article.id == newItem.article.id
                }
                oldItem is NewsListItem.SkeletonItem && newItem is NewsListItem.SkeletonItem -> {
                    true
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: NewsListItem, newItem: NewsListItem): Boolean {
            return oldItem == newItem
        }
    }
}
