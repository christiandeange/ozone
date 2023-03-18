package sh.christian.ozone.error

sealed interface ErrorOutput {
  object Retry : ErrorOutput
  object Dismiss : ErrorOutput
}
