package sh.christian.ozone.api

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.GetProfileResponse
import app.bsky.feed.GetAuthorFeedQueryParams
import app.bsky.feed.GetAuthorFeedResponse
import app.bsky.feed.GetPostThreadQueryParams
import app.bsky.feed.GetPostThreadResponse
import app.bsky.feed.GetTimelineQueryParams
import app.bsky.feed.GetTimelineResponse
import app.bsky.notification.ListNotificationsQueryParams
import app.bsky.notification.ListNotificationsResponse
import com.atproto.repo.CreateRecordRequest
import com.atproto.repo.CreateRecordResponse
import com.atproto.server.CreateAccountRequest
import com.atproto.server.CreateAccountResponse
import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.atproto.server.DescribeServerResponse
import sh.christian.ozone.api.response.AtpResponse

interface AtpApi {
  suspend fun createSession(
    request: CreateSessionRequest,
  ): AtpResponse<CreateSessionResponse>

  suspend fun createAccount(
    request: CreateAccountRequest,
  ): AtpResponse<CreateAccountResponse>

  suspend fun getTimeline(
    params: GetTimelineQueryParams,
  ): AtpResponse<GetTimelineResponse>

  suspend fun getProfile(
    params: GetProfileQueryParams,
  ): AtpResponse<GetProfileResponse>

  suspend fun createRecord(
    request: CreateRecordRequest,
  ): AtpResponse<CreateRecordResponse>

  suspend fun getAuthorFeed(
    params: GetAuthorFeedQueryParams,
  ): AtpResponse<GetAuthorFeedResponse>

  suspend fun getPostThread(
    params: GetPostThreadQueryParams,
  ): AtpResponse<GetPostThreadResponse>

  suspend fun listNotifications(
    params: ListNotificationsQueryParams,
  ): AtpResponse<ListNotificationsResponse>

  suspend fun describeServer(): AtpResponse<DescribeServerResponse>
}
