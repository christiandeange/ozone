package sh.christian.ozone.app

import app.cash.molecule.RecompositionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import software.ralf.app.platform.presenter.compose.launchComposePresenter
import software.ralf.app.platform.scope.RootScopeProvider
import software.ralf.app.platform.scope.Scope
import software.ralf.app.platform.scope.coroutine.CoroutineScopeScoped
import software.ralf.app.platform.scope.coroutine.addCoroutineScopeScoped
import software.ralf.app.platform.scope.coroutine.coroutineScope
import software.ralf.app.platform.scope.coroutine.launch
import kotlin.time.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.compose.ComposePostPresenter
import sh.christian.ozone.error.ErrorPresenter
import sh.christian.ozone.login.LoginPresenter
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.notifications.NotificationsRepository
import sh.christian.ozone.profile.ProfilePresenter
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.timeline.TimelineRepository
import sh.christian.ozone.thread.ThreadPresenter
import sh.christian.ozone.user.MyProfileRepository
import sh.christian.ozone.user.UserDatabase

/** Owns the root Presenter composition for one running Ozone application. */
class OzoneRuntime internal constructor(
  coroutineScope: CoroutineScope,
  presenter: OzonePresenter,
  supervisors: List<Supervisor>,
) : RootScopeProvider {
  override val rootScope: Scope = Scope.buildRootScope("ozone") {
    addCoroutineScopeScoped(
      CoroutineScopeScoped(
        coroutineScope.coroutineContext + SupervisorJob() + CoroutineName("Ozone"),
      )
    )
  }

  private val presenterScope = rootScope.coroutineScope(CoroutineName("OzonePresenter"))

  val templates: StateFlow<OzoneTemplate> = presenterScope.launchComposePresenter(
    presenter = OzoneTemplatePresenter(presenter),
    input = MutableStateFlow(Unit),
    // This runtime is host-owned rather than launched from a Compose coroutine context.
    // ContextClock would require a MonotonicFrameClock that lifecycle and runBlocking scopes lack.
    recompositionMode = RecompositionMode.Immediate,
  ).model

  init {
    supervisors.forEach { supervisor ->
      rootScope.launch { supervisor.start() }
    }
  }

  fun close() = rootScope.destroy()
}

fun initOzone(
  coroutineScope: CoroutineScope,
  storage: PersistentStorage,
): OzoneRuntime {
  val clock = Clock.System
  val serverRepository = ServerRepository(storage)
  val loginRepository = LoginRepository(storage)
  val apiProvider = ApiProvider(serverRepository, loginRepository)
  val userDatabase = UserDatabase(clock, storage, apiProvider)
  val myProfileRepository = MyProfileRepository(apiProvider, userDatabase, loginRepository)
  val timelineRepository = TimelineRepository(apiProvider, loginRepository)
  val notificationsRepository = NotificationsRepository(apiProvider, loginRepository)
  val errorPresenter = ErrorPresenter()
  val presenters = OzonePresenter(
    loginRepository = loginRepository,
    apiProvider = apiProvider,
    loginPresenter = LoginPresenter(serverRepository, apiProvider, errorPresenter),
    timelineRepository = timelineRepository,
    notificationsRepository = notificationsRepository,
    myProfileRepository = myProfileRepository,
    profilePresenter = ProfilePresenter(
      clock = clock,
      apiProvider = apiProvider,
      userDatabase = userDatabase,
      myProfileRepository = myProfileRepository,
      errorPresenter = errorPresenter,
    ),
    threadPresenter = ThreadPresenter(
      clock = clock,
      apiProvider = apiProvider,
      errorPresenter = errorPresenter,
    ),
    composePostPresenter = ComposePostPresenter(
      clock = clock,
      apiProvider = apiProvider,
      userDatabase = userDatabase,
      myProfileRepository = myProfileRepository,
      errorPresenter = errorPresenter,
    ),
    clock = clock,
  )

  return OzoneRuntime(
    coroutineScope = coroutineScope,
    presenter = presenters,
    supervisors = listOf(
      apiProvider,
      myProfileRepository,
      timelineRepository,
      notificationsRepository,
    ),
  )
}
