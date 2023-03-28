package sh.christian.ozone.profile

import app.bsky.actor.GetProfileQueryParams
import app.bsky.actor.ProfileView
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.NetworkWorker
import sh.christian.ozone.api.response.AtpResponse
import sh.christian.ozone.api.response.AtpResponse.Failure
import sh.christian.ozone.api.response.AtpResponse.Success
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.error.ErrorOutput
import sh.christian.ozone.error.ErrorProps
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.error.toErrorProps
import sh.christian.ozone.profile.ProfileState.FetchingProfile
import sh.christian.ozone.profile.ProfileState.ShowingError
import sh.christian.ozone.profile.ProfileState.ShowingProfile
import sh.christian.ozone.ui.compose.TextOverlayScreen
import sh.christian.ozone.ui.workflow.Dismissable
import sh.christian.ozone.ui.workflow.EmptyScreen

class ProfileWorkflow(
  private val apiProvider: ApiProvider,
  private val errorWorkflow: ErrorWorkflow,
) : StatefulWorkflow<ProfileProps, ProfileState, Unit, AppScreen>() {
  override fun initialState(
    props: ProfileProps,
    snapshot: Snapshot?,
  ): ProfileState = FetchingProfile(props.preloadedProfile)

  override fun render(
    renderProps: ProfileProps,
    renderState: ProfileState,
    context: RenderContext,
  ): AppScreen = when (renderState) {
    is FetchingProfile -> {
      context.runningWorker(loadProfile(renderProps.handle)) { result ->
        action {
          val profileView = when (result) {
            is Success -> result.response
            is Failure -> result.response ?: state.profileView
          }
          val errorProps = when (result) {
            is Success -> null
            is Failure -> {
              result.toErrorProps(true)
                ?: ErrorProps.CustomError(
                  "Oops.", "Could not load profile for @${props.handle}.", true
                )
            }
          }

          state = if (errorProps == null && profileView != null) {
            ShowingProfile(profileView)
          } else {
            ShowingError(errorProps!!, profileView)
          }
        }
      }

      val profileView = renderState.profileView
      if (profileView != null) {
        AppScreen(context.profileScreen(renderProps, profileView))
      } else {
        AppScreen(
          EmptyScreen,
          TextOverlayScreen(
            onDismiss = Dismissable.DismissHandler(
              context.eventHandler {
                val currentProfileView = state.profileView
                if (currentProfileView == null) {
                  setOutput(Unit)
                } else {
                  state = ShowingProfile(currentProfileView)
                }
              }
            ),
            text = "Loading @${renderProps.handle}...",
          ),
        )
      }
    }
    is ShowingProfile -> {
      AppScreen(context.profileScreen(renderProps, renderState.profileView))
    }
    is ShowingError -> {
      val mainScreen = renderState.profileView
        ?.let { context.profileScreen(renderProps, it) }
        ?: EmptyScreen

      AppScreen(
        mainScreen,
        context.renderChild(errorWorkflow, renderState.props) { output ->
          action {
            val currentProfileView = state.profileView
            when (output) {
              ErrorOutput.Dismiss -> {
                if (currentProfileView == null) {
                  setOutput(Unit)
                } else {
                  state = ShowingProfile(currentProfileView)
                }
              }
              ErrorOutput.Retry -> {
                state = FetchingProfile(currentProfileView)
              }
            }
          }
        }
      )
    }
  }

  override fun snapshotState(state: ProfileState): Snapshot? = null

  private fun RenderContext.profileScreen(
    props: ProfileProps,
    profileView: ProfileView,
  ): ProfileScreen {
    return ProfileScreen(
      profileView = profileView,
      isSelf = props.isSelf,
      onExit = eventHandler {
        setOutput(Unit)
      },
    )
  }

  private fun loadProfile(handle: String): Worker<AtpResponse<ProfileView>> = NetworkWorker {
    apiProvider.api.getProfile(GetProfileQueryParams(handle))
  }
}
