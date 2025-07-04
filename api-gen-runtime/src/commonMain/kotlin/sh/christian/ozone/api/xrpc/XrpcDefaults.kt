@file:JvmName("XrpcDefaults")

package sh.christian.ozone.api.xrpc

import io.ktor.http.Url
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Base URL for Bluesky Social.
 */
@JvmField
val BSKY_SOCIAL = Url("https://bsky.social")

/**
 * Base URL for Bluesky Network.
 */
@JvmField
val BSKY_NETWORK = Url("https://bsky.network")
