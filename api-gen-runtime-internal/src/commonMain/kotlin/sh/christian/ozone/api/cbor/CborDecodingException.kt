/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 *
 * Copied until https://github.com/Kotlin/kotlinx.serialization/pull/2383 is merged and productionized.
 */
package sh.christian.ozone.api.cbor

import kotlinx.serialization.SerializationException

internal class CborDecodingException(message: String) : SerializationException(message)

internal fun CborDecodingException(expected: String, foundByte: Int) =
  CborDecodingException("Expected $expected, but found ${printByte(foundByte)}")

internal fun printByte(b: Int): String {
  val hexCode = "0123456789ABCDEF"
  return buildString {
    append(hexCode[b shr 4 and 0xF])
    append(hexCode[b and 0xF])
  }
}
