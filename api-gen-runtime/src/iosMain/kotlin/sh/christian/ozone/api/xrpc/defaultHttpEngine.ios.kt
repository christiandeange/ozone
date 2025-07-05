package sh.christian.ozone.api.xrpc

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual val defaultHttpEngine: HttpClientEngineFactory<*> = Darwin
