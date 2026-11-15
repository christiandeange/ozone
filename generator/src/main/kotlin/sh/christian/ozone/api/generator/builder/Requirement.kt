package sh.christian.ozone.api.generator.builder

sealed interface Requirement {
  data class MinValue(
    val minValue: Comparable<*>,
  ) : Requirement

  data class MaxValue(
    val maxValue: Comparable<*>,
  ) : Requirement

  data class MinLength(
    val minLength: Long,
  ) : Requirement

  data class MaxLength(
    val maxLength: Long,
  ) : Requirement

  data class MinStringLength(
    val minLength: Long,
    val needsToString: Boolean,
  ) : Requirement

  data class MaxStringLength(
    val maxLength: Long,
    val needsToString: Boolean,
  ) : Requirement
}
