package com.piaggi.newsapptech.ui.home

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
import com.piaggi.newsapptech.databinding.FragmentHomeBinding
import com.piaggi.newsapptech.ui.adapter.NewsAdapter
import com.piaggi.newsapptech.ui.model.NewsListItem
import com.piaggi.newsapptech.util.NavArgs
import com.piaggi.newsapptech.util.setupPaginationScrollListener
import com.piaggi.newsapptech.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        observeState()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(
            onArticleClick = { article ->
                NavArgs.navigateToArticleDetail(findNavController(), article, R.id.articleDetailFragment)
            },
            onBookmarkClick = { article ->
                viewModel.toggleBookmark(article)
            }
        )

        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
            setupPaginationScrollListener(threshold = 5) {
                viewModel.loadMore()
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
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

    private fun updateUI(state: HomeUIState) {
        binding.swipeRefreshLayout.isRefreshing = state.isRefreshing

        newsAdapter.submitList(state.newsListItems)

        state.error?.let { error ->
            showToast(error)
            viewModel.clearError()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}