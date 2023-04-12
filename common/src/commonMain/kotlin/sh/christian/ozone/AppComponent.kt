package sh.christian.ozone

import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.compose.ComposePostWorkflow
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.home.HomeWorkflow
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.profile.ProfileWorkflow
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.thread.ThreadWorkflow
import sh.christian.ozone.timeline.TimelineRepository
import sh.christian.ozone.timeline.TimelineWorkflow
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserDatabase

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

  private val userDatabase: UserDatabase by lazy {
    UserDatabase(
      clock = clock,
      storage = storage,
      apiProvider = apiProvider,
    )
  }

  private val myProfileRepository: MyProfileRepository by lazy {
    MyProfileRepository(
      apiProvider = apiProvider,
      userDatabase = userDatabase,
    )
  }

  private val timelineRepository: TimelineRepository by lazy {
    TimelineRepository(
      apiProvider = apiProvider,
    )
  }

  private val errorWorkflow: ErrorWorkflow by lazy {
    ErrorWorkflow()
  }

  private val loginWorkflow: LoginWorkflow by lazy {
    LoginWorkflow(
      serverRepository = serverRepository,
      apiProvider = apiProvider,
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

  private val threadWorkflow: ThreadWorkflow by lazy {
    ThreadWorkflow(
      clock = clock,
      apiProvider = apiProvider,
      profileWorkflow = { profileWorkflow },
      errorWorkflow = errorWorkflow,
    )
  }

  private val timelineWorkflow: TimelineWorkflow by lazy {
    TimelineWorkflow(
      clock = clock,
      myProfileRepository = myProfileRepository,
      timelineRepository = timelineRepository,
      errorWorkflow = errorWorkflow,
    )
  }

  private val profileWorkflow: ProfileWorkflow by lazy {
    ProfileWorkflow(
      clock = clock,
      apiProvider = apiProvider,
      userDatabase = userDatabase,
      myProfileRepository = myProfileRepository,
      threadWorkflow = threadWorkflow,
      errorWorkflow = errorWorkflow,
    )
  }

  private val homeWorkflow: HomeWorkflow by lazy {
    HomeWorkflow(
      timelineWorkflow = timelineWorkflow,
      profileWorkflow = profileWorkflow,
      threadWorkflow = threadWorkflow,
      composePostWorkflow = composePostWorkflow,
    )
  }

  val appWorkflow: AppWorkflow by lazy {
    AppWorkflow(
      loginRepository = loginRepository,
      loginWorkflow = loginWorkflow,
      homeWorkflow = homeWorkflow,
    )
  }

  val supervisors: List<Supervisor> by lazy {
    listOf(
      apiProvider,
      myProfileRepository,
    )
  }
}
