@file:Suppress("FunctionName")

package sh.christian.ozone.ui.workflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.squareup.workflow1.RenderingAndSnapshot
import com.squareup.workflow1.SimpleLoggingWorkflowInterceptor
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.renderWorkflowIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <OutputT, RenderingT : Any> WorkflowRendering(
  workflow: Workflow<Unit, OutputT, RenderingT>,
  onOutput: suspend (OutputT) -> Unit,
  content: @Composable (RenderingT) -> Unit,
) = WorkflowRendering(MutableStateFlow(Unit), workflow, onOutput, content)

@Composable
fun <PropsT, OutputT, RenderingT : Any> WorkflowRendering(
  props: PropsT,
  workflow: Workflow<PropsT, OutputT, RenderingT>,
  onOutput: suspend (OutputT) -> Unit,
  content: @Composable (RenderingT) -> Unit,
) = WorkflowRendering(MutableStateFlow(props), workflow, onOutput, content)

@Composable
fun <PropsT, OutputT, RenderingT : Any> WorkflowRendering(
  props: StateFlow<PropsT>,
  workflow: Workflow<PropsT, OutputT, RenderingT>,
  onOutput: suspend (OutputT) -> Unit,
  content: @Composable (RenderingT) -> Unit,
) {
  produceState<RenderingT?>(null, workflow) {
    val renderWorkflow: StateFlow<RenderingAndSnapshot<RenderingT>> = renderWorkflowIn(
      workflow = workflow,
      scope = this,
      initialSnapshot = null,
      interceptors = listOf(SimpleLoggingWorkflowInterceptor()),
      props = props,
      onOutput = onOutput,
    )

    renderWorkflow.collect {
      value = it.rendering
    }

  }.value?.let { rendering -> content(rendering) }
}

@Composable
fun <RenderingT : Any> WorkflowRendering(
  renderings: StateFlow<RenderingT>,
  key: Any = renderings,
  content: @Composable (RenderingT) -> Unit,
) {
  val maybeRendering: RenderingT? by produceState<RenderingT?>(null, key) {
    renderings.collect { value = it }
  }

  maybeRendering?.let { rendering -> content(rendering) }
}
