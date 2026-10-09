package sh.christian.ozone.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import sh.christian.ozone.compose.ComposePostScreen
import sh.christian.ozone.home.HomeScreen
import sh.christian.ozone.login.LoginScreen
import sh.christian.ozone.notifications.NotificationsScreen
import sh.christian.ozone.profile.ProfileScreen
import sh.christian.ozone.settings.SettingsScreen
import sh.christian.ozone.thread.ThreadScreen
import sh.christian.ozone.timeline.TimelineScreen
import sh.christian.ozone.ui.renderer.ScreenModel
import software.ralf.app.platform.inject.ContributesRenderer
import software.ralf.app.platform.renderer.ComposeRenderer

abstract class ScreenRenderer<ModelT : ScreenModel> : ComposeRenderer<ModelT>() {
  @Composable
  final override fun Compose(model: ModelT, modifier: Modifier) = model.Content()
}

@ContributesRenderer class LoginScreenRenderer : ScreenRenderer<LoginScreen>()
@ContributesRenderer class HomeScreenRenderer : ScreenRenderer<HomeScreen>()
@ContributesRenderer class TimelineScreenRenderer : ScreenRenderer<TimelineScreen>()
@ContributesRenderer class NotificationsScreenRenderer : ScreenRenderer<NotificationsScreen>()
@ContributesRenderer class SettingsScreenRenderer : ScreenRenderer<SettingsScreen>()
@ContributesRenderer class ProfileScreenRenderer : ScreenRenderer<ProfileScreen>()
@ContributesRenderer class ThreadScreenRenderer : ScreenRenderer<ThreadScreen>()
@ContributesRenderer class ComposePostScreenRenderer : ScreenRenderer<ComposePostScreen>()
