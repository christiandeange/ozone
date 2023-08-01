/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 *
 * Copied until https://github.com/Kotlin/kotlinx.serialization/pull/2383 is merged and productionized.
 */
package sh.christian.ozone.api.cbor

internal class ByteArrayOutput {
  private var array: ByteArray = ByteArray(32)
  private var position: Int = 0

  private fun ensureCapacity(elementsToAppend: Int) {
    if (position + elementsToAppend <= array.size) {
      return
    }
    val newArray = ByteArray((position + elementsToAppend).takeHighestOneBit() shl 1)
    array.copyInto(newArray)
    array = newArray
  }

  fun toByteArray(): ByteArray {
    val newArray = ByteArray(position)
    array.copyInto(newArray, startIndex = 0, endIndex = this.position)
    return newArray
  }

  fun write(buffer: ByteArray, offset: Int = 0, count: Int = buffer.size) {
    // avoid int overflow
    if (offset < 0 || offset > buffer.size || count < 0
      || count > buffer.size - offset
    ) {
      throw IndexOutOfBoundsException()
    }
    if (count == 0) {
      return
    }

    ensureCapacity(count)
    buffer.copyInto(
      destination = array,
      destinationOffset = this.position,
      startIndex = offset,
      endIndex = offset + count
    )
    this.position += count
  }

  fun write(byteValue: Int) {
    ensureCapacity(1)
    array[position++] = byteValue.toByte()
  }
}
