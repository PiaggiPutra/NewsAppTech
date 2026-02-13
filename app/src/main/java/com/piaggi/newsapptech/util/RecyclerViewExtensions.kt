package com.piaggi.newsapptech.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setupPaginationScrollListener(
    threshold: Int = 5,
    onLoadMore: () -> Unit
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)

            if (dy <= 0) return

            checkLoadMore(rv, threshold, onLoadMore)
        }

        override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
            super.onScrollStateChanged(rv, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                checkLoadMore(rv, threshold, onLoadMore)
            }
        }

        private fun checkLoadMore(recyclerView: RecyclerView, threshold: Int, onLoadMore: () -> Unit) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()

            val isNearBottom = (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
            val allItemsVisible = lastVisibleItemPosition == totalItemCount - 1

            if ((isNearBottom || allItemsVisible)
                && firstVisibleItemPosition >= 0
                && totalItemCount >= threshold
            ) {
                onLoadMore()
            }
        }
    })
}
