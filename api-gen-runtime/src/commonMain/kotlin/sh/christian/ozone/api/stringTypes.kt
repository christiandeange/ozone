package sh.christian.ozone.api

import kotlinx.serialization.Serializable
import sh.christian.ozone.api.runtime.AtIdentifierSerializer
import kotlin.jvm.JvmInline

/**
 * Flexible to any URI schema, following the generic RFC-3986 on URIs. This includes, but isn’t limited to: `did`,
 * `https`, `wss`, `ipfs` (for [Cid]s), `dns`, and of course `at`. Maximum length in Lexicons is 8KB.
 */
@Serializable
@JvmInline
value class Uri(
  val uri: String,
) {
  override fun toString(): String = uri
}

/**
 * The [AT URI scheme](https://atproto.com/specs/at-uri-scheme) (`at://`) makes it easy to reference individual records
 * in a specific repository, identified by either [Did] or [Handle]. AT URIs can also be used to reference a collection
 * within a repository, or an entire repository (aka, an identity).
 */
@Serializable
@JvmInline
value class AtUri(
  val atUri: String,
) {
  override fun toString(): String = atUri
}

/**
 * The AT Protocol uses [Decentralized Identifiers](https://atproto.com/specs/did) (DIDs) as persistent, long-term
 * account identifiers. DID is a W3C standard, with many standardized and proposed DID method implementations.
 */
@Serializable
@JvmInline
value class Did(
  val did: String,
): AtIdentifier {

  init {
    require(Regex.matches(did)) {
      "'$did' is not a valid DID."
    }
  }

  override fun toString(): String = did

  companion object {
    val Regex = Regex("^did:[a-z]+:[a-zA-Z0-9._:%-]*[a-zA-Z0-9._-]$")
  }
}

/**
 * [Handles](https://atproto.com/specs/handle) are a less-permanent identifier for accounts. The mechanism for verifying
 * the link between an account handle and an account [Did] relies on DNS, and possibly connections to a network host, so
 * every handle must be a valid network hostname. Almost every valid "hostname" is also a valid handle, though there are
 * a small number of exceptions.
 */
@Serializable
@JvmInline
value class Handle(
  val handle: String,
): AtIdentifier {

  init {
    require(Regex.matches(handle)) {
      "'$handle' is not a valid handle."
    }
  }

  override fun toString(): String = handle

  companion object {
    val Regex = Regex("^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$")
  }
}

/**
 * A string type which is either a [Did] or a [Handle]. Mostly used in XRPC query parameters. It is unambiguous whether
 * an AtIdentifier is a handle or a DID because a DID always starts with `did:`, and the colon character (`:`) is not
 * an allowed in handles.
 */
@Serializable(with = AtIdentifierSerializer::class)
sealed interface AtIdentifier

/**
 * [Namespaced Identifiers](https://atproto.com/specs/nsid) (NSIDs) are used to reference Lexicon schemas for records,
 * XRPC endpoints, and more. The basic structure and semantics of an NSID are a fully-qualified hostname in Reverse
 * Domain-Name Order, followed by a simple name. The hostname part is the domain authority, and the final segment is the
 * name.
 */
@Serializable
@JvmInline
value class Nsid(
  val nsid: String,
) {
  init {
    require(Regex.matches(nsid)) {
      "'$nsid' is not a valid namespace identifier."
    }
  }

  val domainAuthority: String
    get() = nsid.substringBeforeLast('.')

  val name: String
    get() = nsid.substringAfterLast('.')

  override fun toString(): String = nsid

  companion object {
    val Regex = Regex(
      "^[a-zA-Z]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+" +
          "(\\.[a-zA-Z]([a-zA-Z]{0,61}[a-zA-Z])?)$"
    )
  }
}

/**
 * Links are encoded as [IPFS Content Identifiers](https://atproto.com/specs/data-model#link-and-cid-formats) (CIDs),
 * which have both binary and string representations. CIDs include a metadata code which indicates whether it links to a
 * node (DAG-CBOR) or arbitrary binary data.
 */
@Serializable
@JvmInline
value class Cid(
  val cid: String,
) {
  override fun toString(): String = cid
}

/**
 * An [IETF Language Tag](https://en.wikipedia.org/wiki/IETF_language_tag) string, compliant with
 * [BCP 47](https://www.rfc-editor.org/info/bcp47), defined in [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.txt)
 * ("Tags for Identifying Languages"). This is the same standard used to identify languages in HTTP, HTML, and other web
 * standards. The Lexicon string must validate as a "well-formed" language tag, as defined in the RFC. Clients should
 * ignore language strings which are "well-formed" but not "valid" according to the RFC.
 */
@Serializable
@JvmInline
value class Language(
  val tag: String,
) {
  override fun toString(): String = tag
}

/**
 * [Timestamp identifiers](https://atproto.com/specs/record-key#record-key-type-tid) (sometimes shortened to tid) are
 * the most common record naming scheme. The name is derived from the creation time of the record. Implementations
 * should not rely on global uniqueness of TIDs, and should not trust TID timestamps as record creation timestamps.
 */
@Serializable
@JvmInline
value class Tid(
  val tid: String,
) {
  override fun toString(): String = tid
}

/**
 * A [Record Key](https://atproto.com/specs/record-key) (sometimes shortened to rkey) is used to name and reference an
 * individual record within the same collection of an atproto repository. It ends up as a segment in [AtUri]s, and in
 * the repo MST path. Record Keys are "user-controlled data" and may be arbitrarily selected by hostile accounts.
 */
@Serializable
@JvmInline
value class RKey(
  val rkey: String,
) {
  override fun toString(): String = rkey
}
