package sh.christian.ozone.api.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.runtime.ImmutableListSerializer

/**
 * A generic immutable ordered collection of elements. Supports only read-only access to the immutable list.
 */
typealias ReadOnlyList<T> = @Serializable(with = ImmutableListSerializer::class) ImmutableList<T>
