package sh.christian.ozone.error

import com.squareup.workflow1.StatelessWorkflow
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.ui.workflow.OverlayRendering

@Inject
class ErrorWorkflow : StatelessWorkflow<ErrorProps, ErrorOutput, OverlayRendering>() {
  override fun render(
    renderProps: ErrorProps,
    context: RenderContext,
  ): OverlayRendering {
    return ErrorScreen(
      title = renderProps.title,
      description = renderProps.description,
      retryable = renderProps.retryable,
      onRetry = context.eventHandler { setOutput(ErrorOutput.Retry) },
      onDismiss = context.eventHandler { setOutput(ErrorOutput.Dismiss) },
    )
  }
}
