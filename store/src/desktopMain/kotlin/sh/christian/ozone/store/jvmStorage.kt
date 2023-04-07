package sh.christian.ozone.store

import ca.gosyer.appdirs.AppDirs
import java.nio.file.Path
import java.nio.file.Paths

fun storage(): PersistentStorage {
  val filesDir: Path =
    Paths.get(AppDirs().getUserDataDir("Ozone", "1", "sh.christian.ozone")).resolve("storage")

  return object : KStorePersistentStorage() {
    override fun pathTo(key: String): String {
      return filesDir.resolve("$key.json").toString()
    }
  }
}
