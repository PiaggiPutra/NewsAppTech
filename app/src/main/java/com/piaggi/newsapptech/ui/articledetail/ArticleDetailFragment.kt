package com.piaggi.newsapptech.ui.articledetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.piaggi.newsapptech.R
import com.piaggi.newsapptech.databinding.FragmentArticleDetailBinding
import com.piaggi.newsapptech.domain.entity.Article
import com.piaggi.newsapptech.util.DateUtils
import com.piaggi.newsapptech.util.NavArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleDetailViewModel by viewModels()
    private lateinit var article: Article

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        article = NavArgs.getArticle(requireArguments()) ?: return
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeState()
        viewModel.loadArticle(article)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.article_detail_menu)

            setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.bookmark -> {
                        viewModel.toggleBookmark()
                        true
                    }
                    R.id.share -> {
                        viewModel.shareArticle(requireContext())
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    bindArticle(state.article)
                }
            }
        }
    }

    private fun bindArticle(article: Article?) {
        article ?: return

        binding.apply {
            tvTitle.text = article.title
            tvSource.text = article.source
            tvContent.text = article.content ?: article.description.orEmpty()
            tvPublishedAt.text = DateUtils.formatRelativeTime(parseDate(article.publishedAt))

            Glide.with(ivArticleImage)
                .load(article.urlToImage)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(ivArticleImage)

            updateBookmarkIcon(article.isBookmarked)
        }
    }

    private fun updateBookmarkIcon(isBookmarked: Boolean) {
        binding.toolbar.menu?.findItem(R.id.bookmark)?.let { menuItem ->
            menuItem.setIcon(
                if (isBookmarked) R.drawable.ic_bookmarks
                else R.drawable.ic_bookmarks_outline
            )
        }
    }

    private fun parseDate(dateString: String?): Date? {
        return try {
            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            isoFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}