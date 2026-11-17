@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")

package com.cuso.tailor.view.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.cuso.tailor.utils.PagingUiState

@Composable
fun <T> PaginatedDataList(
    state: PagingUiState<T>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    keySelector: ((T) -> Any?)? = null,
    itemContent: @Composable (T) -> Unit
) {
    // Keep the latest PagingUiState visible inside derivedStateOf
    val currentState by rememberUpdatedState(state)

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 &&
                    lastVisibleItemIndex >= (totalItems - 2) &&
                    !currentState.isLoadingMore &&
                    !currentState.isInitialLoading &&
                    currentState.canLoadMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (state.isInitialLoading && state.items.isEmpty()) {
            ListSkeleton()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = state.items,
                    key = { index, item ->
                        // Avoid item.hashCode() because Gson can assign null to non-nullable DTO fields.
                        // Combining an optional custom key with index ensures uniqueness and zero NPEs.
                        val customKey = try {
                            keySelector?.invoke(item)
                        } catch (_: Exception) {
                            null
                        }
                        if (customKey != null) "${customKey}_$index" else index
                    }
                ) { _, item ->
                    itemContent(item)
                }

                if (state.isLoadingMore) {
                    item(key = "paginated_loading_footer") {
                        ThreeDotLoading()
                    }
                }
            }
        }
    }
}