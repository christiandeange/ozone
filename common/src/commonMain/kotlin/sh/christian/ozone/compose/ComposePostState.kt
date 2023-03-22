package sh.christian.ozone.compose

import sh.christian.ozone.error.ErrorProps

sealed interface ComposePostState {
  object ComposingPost : ComposePostState

  data class CreatingPost(
    val postPayload: PostPayload,
  ) : ComposePostState

  data class ShowingError(
    val errorProps: ErrorProps,
    val postPayload: PostPayload,
  ) : ComposePostState
}
