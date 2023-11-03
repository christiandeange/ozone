package sh.christian.ozone.store

import android.content.Context

val Context.storage: PersistentStorage
  get() {
    val filesDir = filesDir.resolve("storage")

    return object : KStorePersistentStorage() {
      override fun pathTo(key: String): String {
        return filesDir.resolve("$key.json").toString()
      }
    }
  }
