package com.piaggi.newsapptech.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.piaggi.newsapptech.R
import com.piaggi.newsapptech.databinding.FragmentSearchBinding
import com.piaggi.newsapptech.util.NavArgs
import com.piaggi.newsapptech.ui.adapter.NewsAdapter
import com.piaggi.newsapptech.ui.model.NewsListItem
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

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy <= 0) return

                    checkLoadMore()
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        checkLoadMore()
                    }
                }
            })
        }
    }

    private fun checkLoadMore() {
        val layoutManager = binding.rvSearchResult.layoutManager as? LinearLayoutManager ?: return
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

        val isNearBottom = (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
        val allItemsVisible = lastVisibleItemPosition == totalItemCount - 1

        if ((isNearBottom || allItemsVisible)
            && firstVisibleItemPosition >= 0
            && totalItemCount >= 5
        ) {
            viewModel.loadMore()
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
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }

        val articleCount = state.newsListItems.count {
            it is NewsListItem.ArticleItem
        }
        val hasQuery = state.query.isNotEmpty()
        val shouldShowEmpty = hasQuery && articleCount == 0 && !state.isLoading

        binding.tvEmpty.visibility = if (shouldShowEmpty) View.VISIBLE else View.GONE
        binding.rvSearchResult.visibility = if (shouldShowEmpty) View.GONE else View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}