package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent

fun valuesToList(values: List<String>): CodeBlock {
  return codeToList(values.map { value -> CodeBlock.of("%S", value) })
}

fun codeToList(codeBlocks: List<CodeBlock>): CodeBlock {
  if (codeBlocks.isEmpty()) {
    return CodeBlock.of("emptyList()")
  }

  return buildCodeBlock {
    add("listOf(\n")
    withIndent {
      codeBlocks.forEach { code ->
        add(code).add(",\n")
      }
    }
    add(")")
  }
}

fun valuesToMap(values: Map<String, String>): CodeBlock {
  return codeToMap(values.mapValues { (_, value) -> CodeBlock.of("%S", value) })
}

fun codeToMap(codeBlocks: Map<String, CodeBlock>): CodeBlock {
  if (codeBlocks.isEmpty()) {
    return CodeBlock.of("emptyMap()")
  }

  return buildCodeBlock {
    add("mapOf(\n")
    withIndent {
      codeBlocks.forEach { (key, value) ->
        add("%S to ", key).add(value).add(",\n")
      }
    }
    add(")")
  }
}
