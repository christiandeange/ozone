package sh.christian.ozone.api

import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference

@Inject
@SingleInApp
class ServerRepository(
  storage: PersistentStorage,
) {
  private val serverPreference = storage.preference<Server>("servers", Server.BlueskySocial)

  var server: Server by serverPreference

  fun serverFlow(): Flow<Server> = serverPreference.updates
}
