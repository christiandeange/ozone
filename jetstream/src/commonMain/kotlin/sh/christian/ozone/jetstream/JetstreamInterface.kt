package sh.christian.ozone.jetstream

import app.bsky.jetstream.SubscribeMessage
import app.bsky.jetstream.SubscribeOptionsUpdate
import app.bsky.jetstream.SubscribeQueryParams
import kotlinx.coroutines.flow.Flow

interface JetstreamInterface {
  /**
   * Subscribe to a Jetstream firehose with the given [params].
   *
   * Messages will be emitted as they are received via the returned [Flow&lt;SubscribeMessage&gt;][Flow].
   */
  suspend fun subscribe(params: SubscribeQueryParams): Flow<SubscribeMessage>

  /**
   * Subscribe to a Jetstream firehose with the given [params].
   *
   * Messages will be emitted as they are received via [messages][SubscriptionContext.messages]. Options that affect the
   * messages being received can be updated via [updateSubscription()][SubscriptionContext.updateSubscription].
   */
  suspend fun subscribe(
    params: SubscribeQueryParams,
    block: suspend SubscriptionContext.() -> Unit,
  )
}

interface SubscriptionContext {
  /** Messages received from a Jetstream instance. */
  val messages: Flow<SubscribeMessage>

  /** Update the filter options for the current [messages] subscription. */
  suspend fun updateSubscription(message: SubscribeOptionsUpdate)
}
