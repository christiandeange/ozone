package sh.christian.ozone.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

inline fun <T, R> Iterable<T>.mapImmutable(transform: (T) -> R): ImmutableList<R> {
  return map { transform(it) }.toImmutableList()
}

inline fun <T, R> Iterable<T>.flatMapImmutable(transform: (T) -> Iterable<R>): ImmutableList<R> {
  return flatMap { transform(it) }.toImmutableList()
}

fun <T> ImmutableList<T>.plus(iterable: Iterable<T>): ImmutableList<T> {
  return (this + iterable).toImmutableList()
}
