package sh.christian.ozone.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.Serializable

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

/**
 * Returns a list containing the results of applying the given [transform] function to each element in the original
 * collection.
 */
inline fun <T, R> Iterable<T>.mapImmutable(transform: (T) -> R): ImmutableList<R> {
  return map { transform(it) }.toPersistentList()
}

/**
 * Returns a list containing only the non-null results of applying the given [transform] function to each element in the
 * original collection.
 */
inline fun <T, R> Iterable<T>.mapNotNullImmutable(transform: (T) -> R?): ImmutableList<R> {
  return mapNotNull { transform(it) }.toPersistentList()
}

/**
 * Returns a single list of all elements yielded from results of [transform] function being invoked on each element of
 * original collection.
 */
inline fun <T, R> Iterable<T>.flatMapImmutable(transform: (T) -> Iterable<R>): ImmutableList<R> {
  return flatMap { transform(it) }.toPersistentList()
}

/**
 * Returns a list containing all elements of the original collection and then all elements of the given [iterable]
 * collection.
 */
fun <T> ImmutableList<T>.plus(iterable: Iterable<T>): ImmutableList<T> {
  return (this + iterable).toPersistentList()
}
