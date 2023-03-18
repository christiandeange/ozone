package sh.christian.ozone.api

import kotlinx.coroutines.flow.Flow
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference

class ServerRepository(
  storage: PersistentStorage,
) {
  private val serverPreference = storage.preference<Server>("servers", Server.BlueskySocial)

  var server by serverPreference
  fun server(): Flow<Server> = serverPreference.asFlow()
}
