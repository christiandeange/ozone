package sh.christian.ozone.oauth

import io.ktor.util.encodeBase64

internal fun ByteArray.encodeBase64Url(): String {
  return encodeBase64().trimEnd('=').replace('+', '-').replace('/', '_')
}
