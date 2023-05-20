package sh.christian.ozone.timeline

import app.bsky.feed.GetTimelineQueryParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.model.Timeline

@Inject
@SingleInApp
class TimelineRepository(
  private val apiProvider: ApiProvider,
): Supervisor {
  private val latest: MutableStateFlow<Timeline?> = MutableStateFlow(null)
  private val loadErrors: MutableSharedFlow<ErrorProps> = MutableSharedFlow()

  val timeline: Flow<Timeline> = latest.filterNotNull()
  val errors: Flow<ErrorProps> = loadErrors

  override suspend fun CoroutineScope.onStart() {
    apiProvider.auth().filter { it == null }.collect {
      latest.value = null
    }
  }

  suspend fun refresh() {
    load(null)
  }

  suspend fun loadMore() {
    load(latest.value?.cursor)
  }

  private suspend fun load(cursor: String?) {
    val response: AtpResponse<Timeline> = apiProvider.api
      .getTimeline(GetTimelineQueryParams(limit = 100, cursor = cursor))
      .map { Timeline.from(it.feed, it.cursor) }

    when (response) {
      is AtpResponse.Success -> {
        val previousTimeline = latest.value
        val nextTimeline = response.response
        val mergedTimeline = if (cursor != null && previousTimeline != null) {
          Timeline(
            posts = previousTimeline.posts + nextTimeline.posts,
            cursor = nextTimeline.cursor,
          )
        } else {
          nextTimeline
        }

        latest.value = mergedTimeline
      }
      is AtpResponse.Failure -> {
        loadErrors.emit(
          response.toErrorProps(true)
            ?: ErrorProps.CustomError("Oops.", "Could not load timeline", true)
        )
      }
    }
  }
}
