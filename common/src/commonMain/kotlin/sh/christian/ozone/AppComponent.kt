package sh.christian.ozone

import kotlinx.datetime.Clock
import sh.christian.ozone.api.ApiProvider
import sh.christian.ozone.api.ServerRepository
import sh.christian.ozone.app.AppWorkflow
import sh.christian.ozone.app.LoggedInWorkflow
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.error.ErrorWorkflow
import sh.christian.ozone.login.LoginRepository
import sh.christian.ozone.login.LoginWorkflow
import sh.christian.ozone.store.PersistentStorage

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

  private val errorWorkflow: ErrorWorkflow by lazy {
    ErrorWorkflow()
  }

  private val loginWorkflow: LoginWorkflow by lazy {
    LoginWorkflow(
      loginRepository = loginRepository,
      serverRepository = serverRepository,
      apiRepository = apiProvider,
      errorWorkflow = errorWorkflow,
    )
  }

  private val loggedInWorkflow: LoggedInWorkflow by lazy {
    LoggedInWorkflow(
      clock = clock,
      apiProvider = apiProvider,
      errorWorkflow = errorWorkflow,
    )
  }

  val appWorkflow: AppWorkflow by lazy {
    AppWorkflow(
      loginRepository = loginRepository,
      loginWorkflow = loginWorkflow,
      loggedInWorkflow = loggedInWorkflow,
    )
  }

  val supervisors: List<Supervisor> by lazy {
    listOf(
      apiProvider,
    )
  }
}
