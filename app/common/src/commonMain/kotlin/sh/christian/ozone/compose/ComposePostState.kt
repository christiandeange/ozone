package sh.christian.ozone.compose

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.Profile

sealed interface ComposePostState {
  val myProfile: Profile

  data class ComposingPost(
    override val myProfile: Profile,
  ) : ComposePostState

  data class CreatingPost(
    override val myProfile: Profile,
    val postPayload: PostPayload,
  ) : ComposePostState

  data class ShowingError(
    override val myProfile: Profile,
    val errorProps: ErrorProps,
    val postPayload: PostPayload,
  ) : ComposePostState
}
