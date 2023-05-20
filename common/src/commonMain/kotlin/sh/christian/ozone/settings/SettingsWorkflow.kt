package sh.christian.ozone.settings

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.app.AppScreen
import sh.christian.ozone.ui.workflow.ConfirmRendering
import sh.christian.ozone.settings.SettingsOutput.CloseApp
import sh.christian.ozone.settings.SettingsOutput.SignOut
import sh.christian.ozone.settings.SettingsState.ConfirmSignOut
import sh.christian.ozone.settings.SettingsState.ShowingSettings

@Inject
class SettingsWorkflow : StatefulWorkflow<Unit, SettingsState, SettingsOutput, AppScreen>() {
  override fun initialState(
    props: Unit,
    snapshot: Snapshot?
  ): SettingsState = ShowingSettings

  override fun render(
    renderProps: Unit,
    renderState: SettingsState,
    context: RenderContext
  ): AppScreen {
    val settingsScreen = context.settingsScreen()
    val overlay = when (renderState) {
      is ShowingSettings -> null
      is ConfirmSignOut -> context.confirmScreen()
    }

    return AppScreen(
      main = settingsScreen,
      overlay = overlay,
    )
  }

  override fun snapshotState(state: SettingsState): Snapshot? = null

  private fun RenderContext.settingsScreen(): SettingsScreen {
    return SettingsScreen(
      onExit = eventHandler {
        setOutput(CloseApp)
      },
      onSignOut = eventHandler {
        state = ConfirmSignOut
      },
    )
  }

  private fun RenderContext.confirmScreen(): ConfirmRendering {
    return ConfirmRendering(
      title = "Sign Out?",
      description = "Your login credentials will not be saved on this device.",
      onDismiss = eventHandler {
        state = ShowingSettings
      },
      onConfirm = eventHandler {
        setOutput(SignOut)
      },
    )
  }
}
