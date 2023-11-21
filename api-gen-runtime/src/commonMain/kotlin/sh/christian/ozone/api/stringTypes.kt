package sh.christian.ozone.api

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Uri(
  val uri: String,
) {
  override fun toString(): String = uri
}

@Serializable
@JvmInline
value class AtUri(
  val atUri: String,
) {
  override fun toString(): String = atUri
}

@Serializable
@JvmInline
value class Did(
  val did: String,
) {
  override fun toString(): String = did
}

@Serializable
@JvmInline
value class Handle(
  val handle: String,
) {
  override fun toString(): String = handle
}

@Serializable
@JvmInline
value class AtIdentifier(
  val atIdentifier: String,
) {
  override fun toString(): String = atIdentifier
}

@Serializable
@JvmInline
value class Nsid(
  val nsid: String,
) {
  override fun toString(): String = nsid
}

@Serializable
@JvmInline
value class Cid(
  val cid: String,
) {
  override fun toString(): String = cid
}

@Serializable
@JvmInline
value class Language(
  val tag: String,
) {
  override fun toString(): String = tag
}
