package sh.christian.ozone.api.xrpc

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual val defaultHttpEngine: HttpClientEngineFactory<*> = CIO
