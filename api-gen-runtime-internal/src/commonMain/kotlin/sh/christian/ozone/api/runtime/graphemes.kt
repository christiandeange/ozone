package sh.christian.ozone.api.runtime

import tekst.nextGraphemeBoundary

/** Counts Unicode extended grapheme clusters for Lexicon string constraints. */
fun CharSequence.graphemeCount(): Int {
  var count = 0
  var index = 0
  while (index < length) {
    index = nextGraphemeBoundary(index = index, startIndex = index, endIndex = length)
    count++
  }
  return count
}
