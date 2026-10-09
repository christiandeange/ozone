@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:JsModule("zstd-codec")

package sh.christian.ozone.jetstream

import kotlin.js.JsAny

internal external object ZstdCodec {
  fun run(callback: (Zstd) -> Unit)
}

internal external interface Zstd : JsAny

internal external interface ZstdSimple : JsAny {
  fun decompressUsingDict(data: Uint8Array, dict: ZstdDecompressionDictionary): Uint8Array?
}

internal external interface ZstdDecompressionDictionary : JsAny
