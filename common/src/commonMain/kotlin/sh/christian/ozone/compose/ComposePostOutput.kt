package sh.christian.ozone.compose

sealed interface ComposePostOutput {
  object CreatedPost : ComposePostOutput
  object CanceledPost : ComposePostOutput
}
