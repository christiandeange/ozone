package sh.christian.ozone.model

import com.atproto.label.DefsLabel
import kotlinx.serialization.Serializable

@Serializable
data class Label(
  val value: String,
)

fun DefsLabel.toLabel(): Label {
  return Label(
    value = `val`,
  )
}
