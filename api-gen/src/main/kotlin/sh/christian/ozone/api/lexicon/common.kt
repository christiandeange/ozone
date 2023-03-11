package sh.christian.ozone.api.lexicon

sealed interface OneOrMore<T : Any> {
  data class One<T : Any>(
    val value: T,
  ) : OneOrMore<T>

  data class More<T : Any>(
    val values: List<T>,
  ) : OneOrMore<T>

  fun requireOne(): T {
    require(this is One) { "Cannot return single value from $this" }
    return value
  }

  fun values(): List<T> = when (this) {
    is One -> listOf(value)
    is More -> values
  }
}
