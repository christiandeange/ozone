package sh.christian.ozone.api.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable
import sh.christian.ozone.api.runtime.ImmutableListSerializer

/**
 * A generic read-only ordered collection of elements.
 */
typealias ReadOnlyList<T> = @Serializable(with = ImmutableListSerializer::class) ImmutableList<T>

fun <T> Iterable<T>.toReadOnlyList(): ReadOnlyList<T> = toPersistentList()

/**
 * Returns a read-only list containing all elements of this sequence.
 */
fun <T> Sequence<T>.toReadOnlyList(): ReadOnlyList<T> = toPersistentList()

/**
 * Returns a read-only list containing all characters.
 */
fun CharSequence.toReadOnlyList(): ReadOnlyList<Char> = toPersistentList()

/**
 * Returns a new read-only list of the specified elements.
 */
fun <T> readOnlyListOf(vararg elements: T): PersistentList<T> = persistentListOf(*elements)

/**
 * Returns an empty read-only list.
 */
fun <T> readOnlyListOf(): PersistentList<T> = persistentListOf()
