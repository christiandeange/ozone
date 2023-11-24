package sh.christian.ozone.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class Supervisor {
  protected var scope: CoroutineScope? = null

  suspend fun start() {
    coroutineScope {
      scope = this
      onStart()

      try {
        // Hang forever, or until this coroutine is cancelled.
        suspendCancellableCoroutine<Nothing> { }
      } finally {
        scope = null
      }
    }
  }

  protected open suspend fun CoroutineScope.onStart() = Unit

  protected fun requireCoroutineScope(): CoroutineScope = requireNotNull(scope)
}
