/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 *
 * Copied until https://github.com/Kotlin/kotlinx.serialization/pull/2383 is merged and productionized.
 */
package sh.christian.ozone.api.cbor

internal class ByteArrayInput(private var array: ByteArray) {
  private var position: Int = 0
  val availableBytes: Int get() = array.size - position

  fun read(): Int {
    return if (position < array.size) array[position++].toInt() and 0xFF else -1
  }

  fun read(b: ByteArray, offset: Int, length: Int): Int {
    // avoid int overflow
    if (offset < 0 || offset > b.size || length < 0
      || length > b.size - offset
    ) {
      throw IndexOutOfBoundsException()
    }
    // Are there any bytes available?
    if (this.position >= array.size) {
      return -1
    }
    if (length == 0) {
      return 0
    }

    val copied = if (this.array.size - position < length) this.array.size - position else length
    array.copyInto(destination = b, destinationOffset = offset, startIndex = position, endIndex = position + copied)
    position += copied
    return copied
  }

  fun skip(length: Int) {
    position += length
  }
}
