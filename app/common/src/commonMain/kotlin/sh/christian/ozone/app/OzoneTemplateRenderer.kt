package sh.christian.ozone.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import sh.christian.ozone.ui.compose.Overlay
import sh.christian.ozone.error.ErrorPresenter
import sh.christian.ozone.error.ErrorRenderer
import sh.christian.ozone.ui.renderer.ScreenModel
import software.ralf.app.platform.inject.ContributesRenderer
import software.ralf.app.platform.renderer.ComposeRenderer
import software.ralf.app.platform.renderer.Render

@ContributesRenderer
class OzoneTemplateRenderer : ComposeRenderer<OzoneTemplate>() {
  @Composable
  override fun Compose(model: OzoneTemplate, modifier: Modifier) {
    Box(modifier.fillMaxSize()) {
      when (model) {
        is OzoneTemplate.FullScreen -> RenderContent(model.content)
        is OzoneTemplate.Modal -> {
          RenderContent(model.content)
          Modal(model.modal)
        }
      }
    }
  }

  @Composable
  private fun Modal(model: OzoneModalModel) {
    val visibility = remember(model) { MutableTransitionState(false) }
    var wasVisible by remember(model) { mutableStateOf(false) }
    Overlay(
      modifier = Modifier.fillMaxSize(),
      visibleState = visibility,
      enter = EnterTransition.None,
      exit = ExitTransition.None,
      onClickOutside = { visibility.targetState = false },
    ) {
      RenderContent(model)
    }
    LaunchedEffect(model) { visibility.targetState = true }
    LaunchedEffect(visibility.currentState) {
      if (visibility.currentState) wasVisible = true
    }
    LaunchedEffect(visibility.currentState, visibility.targetState, wasVisible) {
      if (wasVisible && !visibility.currentState && !visibility.targetState) {
        model.onRequestDismiss?.invoke()
      }
    }
  }

  @Composable
  private fun RenderContent(model: software.ralf.app.platform.presenter.BaseModel) {
    when (model) {
      is ScreenModel -> model.Content()
      is ErrorPresenter.Model -> ErrorRenderer().renderCompose(model)
      is ImageModalModel -> ImageModalRenderer().renderCompose(model)
      else -> Render(model)
    }
  }
}
