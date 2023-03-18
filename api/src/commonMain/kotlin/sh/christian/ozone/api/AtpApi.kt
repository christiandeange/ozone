package sh.christian.ozone.api

import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import com.atproto.session.CreateRequest
import com.atproto.session.CreateResponse
import sh.christian.ozone.api.response.AtpResponse

interface AtpApi {
  suspend fun createSession(
    request: CreateRequest,
  ): AtpResponse<CreateResponse>

  suspend fun getTimeline(
    params: GetTimelineQueryParams,
  ): AtpResponse<GetTimelineResponse>
}
