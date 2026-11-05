@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")

package com.cuso.tailor.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PagingUiState<T>(
    val items: List<T> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1,
    val error: String? = null
)

class PagingHelper<T>(
    private val scope: CoroutineScope,
    private val pageSize: Int = 10,
    private val fetchData: suspend (page: Int, limit: Int) -> Result<Pair<List<T>, Int>> // Returns (Items, TotalCount)
) {
    private val _state = MutableStateFlow(PagingUiState<T>())
    val state: StateFlow<PagingUiState<T>> = _state.asStateFlow()

    fun refresh() {
        if (_state.value.isInitialLoading) return
        scope.launch {
            _state.update { it.copy(isInitialLoading = true, error = null, currentPage = 1, canLoadMore = true) }
            fetchData(1, pageSize).fold(
                onSuccess = { (newItems, totalCount) ->
                    _state.update {
                        it.copy(
                            items = newItems,
                            isInitialLoading = false,
                            canLoadMore = newItems.size < totalCount && newItems.isNotEmpty(),
                            currentPage = 1
                        )
                    }
                },
                onFailure = { err ->
                    _state.update { it.copy(isInitialLoading = false, error = err.message) }
                }
            )
        }
    }

    fun loadNextPage() {
        val current = _state.value
        if (current.isLoadingMore || current.isInitialLoading || !current.canLoadMore) return

        scope.launch {
            _state.update { it.copy(isLoadingMore = true, error = null) }
            val nextPage = current.currentPage + 1

            fetchData(nextPage, pageSize).fold(
                onSuccess = { (newItems, totalCount) ->
                    _state.update {
                        val combined = (it.items + newItems)
                        it.copy(
                            items = combined,
                            isLoadingMore = false,
                            currentPage = nextPage,
                            canLoadMore = combined.size < totalCount && newItems.isNotEmpty()
                        )
                    }
                },
                onFailure = { err ->
                    _state.update { it.copy(isLoadingMore = false, error = err.message) }
                }
            )
        }
    }
}