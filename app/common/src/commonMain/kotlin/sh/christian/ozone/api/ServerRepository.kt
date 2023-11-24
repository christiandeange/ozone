package sh.christian.ozone.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import sh.christian.ozone.app.Supervisor
import sh.christian.ozone.di.SingleInApp
import sh.christian.ozone.login.auth.Server
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference

@Inject
@SingleInApp
class ServerRepository(
  storage: PersistentStorage,
) : Supervisor() {
  private val serverPreference = storage.preference<Server>("servers", Server.BlueskySocial)

  fun setServer(server: Server) {
    requireCoroutineScope().launch {
      serverPreference.set(server)
    }
  }

  fun server(): Flow<Server> = serverPreference.updates.filterNotNull()
}
