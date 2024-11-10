package sh.christian.ozone.api.model

/**
 * Parent interface for all enums in the API, which holds the original enum name.
 */
abstract class AtpEnum {
  abstract val value: String

  override fun toString(): String = value
}
