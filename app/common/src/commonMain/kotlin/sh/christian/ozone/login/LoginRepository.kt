package sh.christian.ozone.login

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference

@Inject
@SingleInApp
class LoginRepository(
  storage: PersistentStorage,
) : Supervisor() {
  private val authPreference = storage.preference<AuthInfo>("auth-info", null)

  fun setAuth(authInfo: AuthInfo?) {
    requireCoroutineScope().launch {
      authPreference.set(authInfo)
    }
  }

  fun auth(): Flow<AuthInfo?> = authPreference.updates
}
