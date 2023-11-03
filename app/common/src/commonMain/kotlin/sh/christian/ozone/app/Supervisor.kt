package sh.christian.ozone.app

import kotlinx.coroutines.CoroutineScope

interface Supervisor {
  suspend fun CoroutineScope.onStart()
}
