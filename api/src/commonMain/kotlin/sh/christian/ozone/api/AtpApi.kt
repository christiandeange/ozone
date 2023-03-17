package sh.christian.ozone.api

import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetAuthorFeedResponse
import com.atproto.session.CreateRequest
import com.atproto.session.CreateResponse
import sh.christian.ozone.api.response.AtpResponse

interface AtpApi {
  suspend fun createSession(
    request: CreateRequest,
  ): AtpResponse<CreateResponse>

  suspend fun getUserFeed(
    params: GetAuthorFeedQueryParams,
  ): AtpResponse<GetAuthorFeedResponse>
}
