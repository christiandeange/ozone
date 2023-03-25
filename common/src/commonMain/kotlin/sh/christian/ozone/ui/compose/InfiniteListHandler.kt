package sh.christian.ozone.ui.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun InfiniteListHandler(
  state: LazyListState,
  buffer: Int = 10,
  onLoadMore: () -> Unit,
) {
  val loadMore: State<Boolean> = remember {
    derivedStateOf {
      val layoutInfo = state.layoutInfo
      val totalItems = layoutInfo.totalItemsCount
      val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

      lastVisibleItemIndex > (totalItems - buffer)
    }
  }

  LaunchedEffect(Unit) {
    snapshotFlow { loadMore.value }
      .distinctUntilChanged()
      .filter { it }
      .collect { onLoadMore() }
  }
}
