package sh.christian.ozone.store

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import java.io.File

fun storage(
  coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob(),
): PersistentStorage {
  val folder = File(System.getProperty("user.home")).resolve(".ozone")
  return DataStoreStorage(
    PreferenceDataStoreFactory.create(
      scope = coroutineScope,
      produceFile = {
        folder.also { it.mkdirs() }.resolve("settings.preferences_pb")
      }
    )
  )
}
