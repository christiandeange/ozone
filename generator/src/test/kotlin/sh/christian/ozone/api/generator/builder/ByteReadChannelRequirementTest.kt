package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import sh.christian.ozone.api.generator.BinaryDataType
import sh.christian.ozone.api.generator.className
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the `ByteReadChannel` length-requirement handling in [createDataClass]. `ByteReadChannel`
 * has no `count()`, so emitting a `MaxLength`/`MinLength` `require(... .count() ...)` check for such
 * a field would produce code that doesn't compile — those checks must be skipped, while the same
 * checks for `ByteArray` and `List` fields (which do have `count()`) must be preserved.
 */
class ByteReadChannelRequirementTest {
  private val byteReadChannel: TypeName = BinaryDataType.ByteReadChannel.className()

  private fun render(type: TypeName, nullable: Boolean): String {
    val property = SimpleProperty(
      name = "blob",
      type = type,
      nullable = nullable,
      description = null,
      definedDefault = null,
      requirements = listOf(Requirement.MaxLength(10_000L)),
    )
    return createDataClass(ClassName("com.example", "Foo"), listOf(property), null).toString()
  }

  @Test
  fun `byteReadChannel skips count check`() =
    assertFalse(render(byteReadChannel, nullable = false).contains(".count()"))

  @Test
  fun `nullable byteReadChannel skips count check`() =
    assertFalse(render(byteReadChannel, nullable = true).contains(".count()"))

  @Test
  fun `byteArray still enforces count check`() =
    assertTrue(render(BYTE_ARRAY, nullable = false).contains("blob.count()"))

  @Test
  fun `list of byteReadChannel still enforces count check`() =
    assertTrue(render(LIST.parameterizedBy(byteReadChannel), nullable = false).contains("blob.count()"))
}
