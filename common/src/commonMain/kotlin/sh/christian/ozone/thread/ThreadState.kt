package sh.christian.ozone.thread

import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.model.Thread
import sh.christian.ozone.profile.ProfileProps
import sh.christian.ozone.ui.compose.OpenImageAction

sealed interface ThreadState {
  val thread: Thread?
  val previousState: ThreadState?

  data class FetchingPost(
    override val thread: Thread?,
    override val previousState: ThreadState?,
    val uri: String,
  ) : ThreadState

  data class ShowingPost(
    override val thread: Thread,
    override val previousState: ThreadState?,
  ) : ThreadState

  data class ShowingProfile(
    override val previousState: ThreadState,
    val props: ProfileProps,
  ) : ThreadState by previousState

  data class ShowingFullSizeImage(
    override val previousState: ThreadState,
    val openImageAction: OpenImageAction,
  ) : ThreadState by previousState
//
//  data class ComposingReply(
//    val previousState: ThreadState,
//    val props: ComposePostProps,
//  ) : ThreadState by previousState

  data class ShowingError(
    override val previousState: ThreadState,
    val props: ErrorProps,
  ) : ThreadState by previousState
}
