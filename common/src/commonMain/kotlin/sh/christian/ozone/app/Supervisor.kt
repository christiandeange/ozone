package sh.christian.ozone.app

import kotlinx.coroutines.CoroutineScope

interface Supervisor {
  fun CoroutineScope.onStart()
}
