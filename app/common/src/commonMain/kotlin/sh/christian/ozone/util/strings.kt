package sh.christian.ozone.util

import sh.christian.ozone.ui.compose.codepoints

/**
 * Returns a mapping of byte offsets to character offsets.
 * Assumes that you are providing a valid UTF-8 string as input.
 * Text encodings are really a lot of fun.
 */
fun String.byteOffsets(): List<Int> {
  val string = this
  return buildList {
    var i = 0
    string.codepoints().forEach { code ->
      if (string[i].isLowSurrogate()) i++

      add(i)
      if (code >= TWOBYTES) {
        add(i)
        if (code >= THREEBYTES) {
          add(i)
          if (code >= FOURBYTES) {
            add(i)
          }
        }
      }
      i++
    }
    if (!isEmpty()) {
      add(this@buildList.last() + 1)
    }
  }
}

private const val TWOBYTES = 0x80 // 128
private const val THREEBYTES = 0x800 // 2048
private const val FOURBYTES = 0x10000 // 65536
