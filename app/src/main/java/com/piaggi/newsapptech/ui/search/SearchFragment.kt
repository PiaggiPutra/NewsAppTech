package com.piaggi.newsapptech.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.piaggi.newsapptech.R
import com.piaggi.newsapptech.databinding.FragmentSearchBinding
import com.piaggi.newsapptech.util.NavArgs
import com.piaggi.newsapptech.ui.adapter.NewsAdapter
import com.piaggi.newsapptech.ui.model.NewsListItem
import com.piaggi.newsapptech.util.hideKeyboard
import com.piaggi.newsapptech.util.setVisibleIf
import com.piaggi.newsapptech.util.setupPaginationScrollListener
import com.piaggi.newsapptech.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            onArticleClick = { article ->
                NavArgs.navigateToArticleDetail(findNavController(), article, R.id.articleDetailFragment)
            },
            onBookmarkClick = { article ->
                viewModel.toggleBookmark(article)
            }
        )

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
            setupPaginationScrollListener(threshold = 5) {
                viewModel.loadMore()
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setQueryHint(getString(R.string.search_news))
            setIconifiedByDefault(false)

            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    hideKeyboard()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onQueryChange(newText.orEmpty())
                    return true
                }
            })
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: SearchUIState) {
        adapter.submitList(state.newsListItems)

        state.error?.let { error ->
            showToast(error)
            viewModel.clearError()
        }

        val articleCount = state.newsListItems.count {
            it is NewsListItem.ArticleItem
        }
        val hasQuery = state.query.isNotEmpty()
        val shouldShowEmpty = hasQuery && articleCount == 0 && !state.isLoading

        binding.tvEmpty.setVisibleIf(shouldShowEmpty)
        binding.rvSearchResult.setVisibleIf(!shouldShowEmpty)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}