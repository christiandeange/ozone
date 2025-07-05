package sh.christian.ozone.api.xrpc

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual val defaultHttpEngine: HttpClientEngineFactory<*> = Js
