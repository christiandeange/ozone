package sh.christian.ozone

import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.profile.ProfileWorkflow
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.timeline.ProfileRepository
import sh.christian.ozone.timeline.TimelineWorkflow

class AppComponent(
  private val storage: PersistentStorage,
) {
  private val serverRepository: ServerRepository by lazy {
    ServerRepository(storage)
  }

  private val loginRepository: LoginRepository by lazy {
    LoginRepository(storage)
  }

  private val apiProvider: ApiProvider by lazy {
    ApiProvider(serverRepository, loginRepository)
  }

  private val clock: Clock by lazy {
    Clock.System
  }

  private val profileRepository: ProfileRepository by lazy {
    ProfileRepository(
      apiProvider = apiProvider,
    )
  }

  private val errorWorkflow: ErrorWorkflow by lazy {
    ErrorWorkflow()
  }

  private val loginWorkflow: LoginWorkflow by lazy {
    LoginWorkflow(
      serverRepository = serverRepository,
      apiRepository = apiProvider,
      errorWorkflow = errorWorkflow,
    )
  }

  private val composePostWorkflow: ComposePostWorkflow by lazy {
    ComposePostWorkflow(
      clock = clock,
      apiProvider = apiProvider,
      errorWorkflow = errorWorkflow,
    )
  }

  private val timelineWorkflow: TimelineWorkflow by lazy {
    TimelineWorkflow(
      clock = clock,
      apiProvider = apiProvider,
      profileRepository = profileRepository,
      composePostWorkflow = composePostWorkflow,
      profileWorkflow = profileWorkflow,
      errorWorkflow = errorWorkflow,
    )
  }

  private val profileWorkflow: ProfileWorkflow by lazy {
    ProfileWorkflow(
      clock = clock,
      apiProvider = apiProvider,
      errorWorkflow = errorWorkflow,
    )
  }

  val appWorkflow: AppWorkflow by lazy {
    AppWorkflow(
      loginRepository = loginRepository,
      loginWorkflow = loginWorkflow,
      timelineWorkflow = timelineWorkflow,
    )
  }

  val supervisors: List<Supervisor> by lazy {
    listOf(
      apiProvider,
      profileRepository,
    )
  }
}
