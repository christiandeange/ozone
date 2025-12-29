package sh.christian.ozone.api.model.permissions

import kotlinx.serialization.SerialName
import sh.christian.ozone.api.Language

/**
 * A set of permissions that can be granted to an application.
 */
@SerialName("permission-set")
abstract class PermissionSet(
  /**
   * The default title of the permission set, typically in English.
   */
  val defaultTitle: String? = null,
  private val localizedTitles: Map<String, String> = emptyMap(),
  /**
   * The default detail message of the permission set, typically in English.
   */
  val defaultDetail: String? = null,
  private val localizedDetails: Map<String, String> = emptyMap(),
  /**
   * The list of permissions that are part of this permission set.
   */
  val permissions: List<Permission> = emptyList(),
) {
  /**
   * Get the title for a specific language tag, falling back to the default title if not available.
   */
  fun titleForLanguage(language: Language): String? = titleForLanguage(language.tag)

  /**
   * Get the title for a specific language tag, falling back to the default title if not available.
   *
   * The language tag should be an [IETF Language Tag](https://en.wikipedia.org/wiki/IETF_language_tag) string,
   * compliant with [BCP 47](https://www.rfc-editor.org/info/bcp47), defined in
   * [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.txt) ("Tags for Identifying Languages").
   */
  fun titleForLanguage(languageTag: String): String? {
    return localizedTitles[languageTag]
      ?: localizedTitles[languageTag.substringBefore('-')]
      ?: defaultTitle
  }

  /**
   * Get the detail message for a specific language tag, falling back to the default detail message if not available.
   */
  fun detailForLanguage(language: Language): String? = detailForLanguage(language.tag)

  /**
   * Get the detail message for a specific language tag, falling back to the default detail message if not available.
   *
   * The language tag should be an [IETF Language Tag](https://en.wikipedia.org/wiki/IETF_language_tag) string,
   * compliant with [BCP 47](https://www.rfc-editor.org/info/bcp47), defined in
   * [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.txt) ("Tags for Identifying Languages").
   */
  fun detailForLanguage(languageTag: String): String? {
    return localizedDetails[languageTag]
      ?: localizedDetails[languageTag.substringBefore('-')]
      ?: defaultDetail
  }
}
