package sh.christian.ozone.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import sh.christian.ozone.di.AppComponent
import sh.christian.ozone.di.create
import sh.christian.ozone.store.PersistentStorage

fun initWorkflow(
  coroutineScope: CoroutineScope,
  storage: PersistentStorage,
): AppWorkflow {
  val component = AppComponent::class.create(storage)
  val workflow = component.appWorkflow

  component.supervisors.forEach {
    coroutineScope.launch(SupervisorJob()) { it.start() }
  }

  return workflow
}
