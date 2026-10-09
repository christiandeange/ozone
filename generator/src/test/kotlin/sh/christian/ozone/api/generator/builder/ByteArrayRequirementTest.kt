package sh.christian.ozone.api.generator.builder

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies that length requirements on `ByteArray` binary fields are still emitted as
 * `count()`-based `require(...)` checks. This is the counterpart to [ByteReadChannelRequirementTest],
 * which asserts those same checks are skipped for `ByteReadChannel` (a type that has no `count()`).
 *
 * Asserted bounds are kept under 1000 so they render verbatim — kotlinpoet inserts digit-group
 * underscores (e.g. `1_000`) for larger literals.
 */
class ByteArrayRequirementTest {
  private fun render(nullable: Boolean, vararg requirements: Requirement): String {
    val property = SimpleProperty(
      name = "blob",
      type = BYTE_ARRAY,
      nullable = nullable,
      description = null,
      definedDefault = null,
      requirements = requirements.toList(),
    )
    return createDataClass(ClassName("com.example", "Foo"), listOf(property), null).toString()
  }

  @Test
  fun `maxLength emits count check`() {
    val out = render(nullable = false, Requirement.MaxLength(200L))
    assertTrue(out.contains("require(blob.count() <= 200)"), out)
  }

  @Test
  fun `minLength emits count check`() {
    val out = render(nullable = false, Requirement.MinLength(5L))
    assertTrue(out.contains("require(blob.count() >= 5)"), out)
  }

  @Test
  fun `min and max length both emit checks`() {
    val out = render(nullable = false, Requirement.MinLength(5L), Requirement.MaxLength(200L))
    assertTrue(out.contains("blob.count() >= 5"), out)
    assertTrue(out.contains("blob.count() <= 200"), out)
  }

  @Test
  fun `nullable maxLength guards against null`() {
    val out = render(nullable = true, Requirement.MaxLength(200L))
    assertTrue(out.contains("require(blob == null || blob.count() <= 200)"), out)
  }
}
