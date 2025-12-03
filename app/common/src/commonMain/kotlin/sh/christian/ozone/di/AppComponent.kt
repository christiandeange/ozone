package sh.christian.ozone.di

import kotlin.time.Clock
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.notifications.NotificationsRepository
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.timeline.TimelineRepository
import sh.christian.ozone.user.MyProfileRepository

@Component
@SingleInApp
abstract class AppComponent(
  @get:Provides @get:SingleInApp protected val storage: PersistentStorage,
) {
  abstract val appWorkflow: AppWorkflow

  abstract val supervisors: Set<Supervisor>

  @Provides
  fun clock(): Clock = Clock.System

  protected val ApiProvider.bind: Supervisor
    @Provides @IntoSet get() = this

  protected val MyProfileRepository.bind: Supervisor
    @Provides @IntoSet get() = this

  protected val TimelineRepository.bind: Supervisor
    @Provides @IntoSet get() = this

  protected val NotificationsRepository.bind: Supervisor
    @Provides @IntoSet get() = this
}
