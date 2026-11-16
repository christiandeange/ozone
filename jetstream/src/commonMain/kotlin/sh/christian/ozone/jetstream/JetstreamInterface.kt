package sh.christian.ozone.jetstream

import kotlinx.coroutines.flow.Flow
import network.bsky.jetstream.GetBlockQueryParams
import network.bsky.jetstream.GetSegmentQueryParams
import network.bsky.jetstream.ListSegmentsQueryParams
import network.bsky.jetstream.ListSegmentsResponse
import network.bsky.jetstream.PlanSnapshotRequest
import network.bsky.jetstream.PlanSnapshotResponse
import network.bsky.jetstream.SubscribeMessage
import network.bsky.jetstream.SubscribeOptionsUpdate
import network.bsky.jetstream.SubscribeQueryParams

interface JetstreamInterface {
  /** Subscribe to a Jetstream firehose. */
  suspend fun subscribe(
    params: SubscribeQueryParams,
  ): Flow<SubscribeMessage>

  /**
   * Subscribe to a Jetstream firehose with the given [params].
   *
   * Messages will be emitted as they are received via [messages][SubscriptionContext.messages].
   * Options that affect the messages being received can be updated via
   * [updateSubscription()][SubscriptionContext.updateSubscription].
   */
  suspend fun subscribe(
    params: SubscribeQueryParams,
    block: suspend SubscriptionContext.() -> Unit,
  )

  /** List sealed archive segments. */
  suspend fun listSegments(
    params: ListSegmentsQueryParams = ListSegmentsQueryParams(),
  ): ListSegmentsResponse

  /** Plan a point-in-time archive snapshot. */
  suspend fun planSnapshot(
    request: PlanSnapshotRequest = PlanSnapshotRequest(),
  ): PlanSnapshotResponse

  /** Download a complete sealed archive segment. */
  suspend fun getSegment(
    params: GetSegmentQueryParams,
  ): ByteArray

  /** Download one zstd-compressed block from a sealed archive segment. */
  suspend fun getBlock(
    params: GetBlockQueryParams,
  ): ByteArray
}

interface SubscriptionContext {
  /** Messages received from a Jetstream instance. */
  val messages: Flow<SubscribeMessage>

  /** Update the filter options for the current [messages] subscription. */
  suspend fun updateSubscription(message: SubscribeOptionsUpdate)
}
