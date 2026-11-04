package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestPage

/**
 * Paging of a request list: whether the server has more requests and whether the next ones are loading.
 */
data class PagingState(
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadMoreFailed: Boolean = false
) {
    val canLoadMore: Boolean get() = hasMore && !isLoadingMore

    companion object {
        /** Paging after a page was received from the server. */
        fun after(page: RequestPage): PagingState = PagingState(hasMore = page.hasNext)

        /** Page to ask for when the list already shows [loadedCount] requests. */
        fun nextPage(loadedCount: Int): Int = loadedCount / RequestPage.DEFAULT_SIZE

        /** Adds the requests of [page] at the end of [current], skipping the ones already shown. */
        fun merge(current: List<Request>, page: RequestPage): List<Request> =
            (current + page.items).distinctBy { it.id }
    }
}
