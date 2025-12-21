package sh.christian.ozone.api.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.runtime.LenientInstantIso8601Serializer

/**
 * A specific moment in time.
 */
typealias Timestamp = @Serializable(LenientInstantIso8601Serializer::class) Instant