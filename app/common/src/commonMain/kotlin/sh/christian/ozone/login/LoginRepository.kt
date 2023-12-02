package sh.christian.ozone.login

import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.login.auth.AuthInfo
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.nullablePreference

@Inject
@SingleInApp
class LoginRepository(
  storage: PersistentStorage,
) {
  private val authPreference = storage.nullablePreference<AuthInfo>("auth-info", null)

  var auth: AuthInfo? by authPreference

  fun authFlow(): Flow<AuthInfo?> = authPreference.updates
}
