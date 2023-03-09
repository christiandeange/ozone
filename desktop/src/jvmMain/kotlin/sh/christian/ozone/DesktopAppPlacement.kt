package sh.christian.ozone

import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowPosition.Absolute
import androidx.compose.ui.window.WindowPosition.Aligned
import androidx.compose.ui.window.WindowPosition.PlatformDefault
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.Serializer

class DesktopAppPlacement(
  storage: PersistentStorage,
) {
  private val sizePreference =
    storage.preference("window-size", DEFAULT_SIZE, DpSizeSerializer)

  private val positionPreference =
    storage.preference("window-position", DEFAULT_POSITION, WindowPositionSerializer)

  var size: DpSize
    get() = sizePreference.get()
    set(value) {
      sizePreference.set(value)
    }

  var position: WindowPosition
    get() = positionPreference.get()
    set(value) {
      positionPreference.set(value)
    }

  companion object {
    val DEFAULT_SIZE: DpSize = DpSize(450.dp, 800.dp)
    val DEFAULT_POSITION: WindowPosition = WindowPosition(Center)
  }
}

private object DpSizeSerializer : Serializer<DpSize> {
  override fun deserialize(serialized: String): DpSize {
    val (width, height) = serialized.split(":")
    return DpSize(width.toFloat().dp, height.toFloat().dp)
  }

  override fun serialize(value: DpSize): String {
    return "${value.width.value}:${value.height.value}"
  }
}

private object WindowPositionSerializer : Serializer<WindowPosition> {
  override fun deserialize(serialized: String): WindowPosition {
    val values = serialized.split(":")

    return when (values.first()) {
      "PlatformDefault" -> {
        PlatformDefault
      }

      "Absolute" -> {
        Absolute(values[1].toFloat().dp, values[2].toFloat().dp)
      }

      "Aligned" -> {
        Aligned(
          when (values[1]) {
            "BiasAlignment" -> {
              BiasAlignment(values[2].toFloat(), values[3].toFloat())
            }

            "BiasAbsoluteAlignment" -> {
              BiasAbsoluteAlignment(values[2].toFloat(), values[3].toFloat())
            }

            else -> error("Unable to deserialize ${values.drop(1)}.")
          }
        )
      }

      else -> error("Unknown WindowPosition: $values")
    }
  }

  override fun serialize(value: WindowPosition): String {
    return when (value) {
      is PlatformDefault -> {
        "PlatformDefault"
      }

      is Absolute -> {
        "Absolute:${value.x.value}:${value.y.value}"
      }

      is Aligned -> {
        when (val alignment = value.alignment) {
          is BiasAlignment -> {
            "Aligned:BiasAlignment:${alignment.horizontalBias}:${alignment.verticalBias}"
          }

          is BiasAbsoluteAlignment -> {
            // Hacky trick to get the original (private) member fields.
            val onePlusAlignments = alignment.align(IntSize(0, 0), IntSize(2, 2), Ltr)
            val xAlign = onePlusAlignments.x - 1
            val yAlign = onePlusAlignments.y - 1

            "Aligned:BiasAbsoluteAlignment:$xAlign:$yAlign"
          }

          else -> {
            error("Unable to serialize $alignment.")
          }
        }
      }
    }
  }
}
