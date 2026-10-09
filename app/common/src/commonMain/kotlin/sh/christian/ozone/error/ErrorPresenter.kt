package sh.christian.ozone.error

import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Inject
import software.ralf.app.platform.presenter.compose.ComposePresenter
import sh.christian.ozone.app.OzoneModalModel

/** Presents an error as an app-level modal with explicit retry and dismiss callbacks. */
@Inject
class ErrorPresenter : ComposePresenter<ErrorPresenter.Input, ErrorPresenter.Model> {
  @Composable
  override fun present(input: Input): Model = Model(
    title = input.props.title,
    description = input.props.description,
    retryable = input.props.retryable,
    onRetry = { input.onOutput(ErrorOutput.Retry) },
    onRequestDismiss = { input.onOutput(ErrorOutput.Dismiss) },
  )

  data class Input(
    val props: ErrorProps,
    val onOutput: (ErrorOutput) -> Unit,
  )

  data class Model(
    val title: String?,
    val description: String?,
    val retryable: Boolean,
    val onRetry: () -> Unit,
    override val onRequestDismiss: (() -> Unit)?,
  ) : OzoneModalModel
}
