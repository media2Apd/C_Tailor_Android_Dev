@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")

package com.cuso.tailor.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cuso.tailor.utils.PagingUiState

@Composable
fun <T> PaginatedDataList(
    state: PagingUiState<T>,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T) -> Unit
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            lastVisibleItemIndex >= (totalItems - 2) &&
                    !state.isLoadingMore &&
                    !state.isInitialLoading &&
                    state.canLoadMore
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
                items(
                    items = state.items,
                    key = { item -> item.hashCode() }
                ) { item ->
                    itemContent(item)
                }

                if (state.isLoadingMore) {
                    item {
                       ThreeDotLoading()
                    }
                }
            }
        }
    }
}