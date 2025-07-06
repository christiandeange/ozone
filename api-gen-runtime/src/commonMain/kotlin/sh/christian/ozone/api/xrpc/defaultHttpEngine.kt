package sh.christian.ozone.api.xrpc

import io.ktor.client.engine.HttpClientEngineFactory

/**
 * Platform-default HTTP engine for network requests.
 */
expect val defaultHttpEngine: HttpClientEngineFactory<*>
