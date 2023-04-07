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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import sh.christian.ozone.store.PersistentStorage
import sh.christian.ozone.store.preference

class DesktopAppPlacement(
  storage: PersistentStorage,
) {
  private val sizePreference = storage.preference("window-size", DEFAULT_SIZE)
  private val positionPreference = storage.preference("window-position", DEFAULT_POSITION)

  var size: DpSize
    get() = runBlocking { sizePreference.get()!!.toDpSize() }
    set(value) {
      runBlocking { sizePreference.set(WindowSize(value)) }
    }

  var position: WindowPosition
    get() = runBlocking { positionPreference.get()!!.toWindowPosition() }
    set(value) {
      runBlocking { positionPreference.set(WindowZone(value)) }
    }

  private companion object {
    val DEFAULT_SIZE = WindowSize(DpSize(450.dp, 800.dp))
    val DEFAULT_POSITION = WindowZone(WindowPosition(Center))
  }
}

@Serializable
private data class WindowSize(
  val widthDp: Float,
  val heightDp: Float,
) {
  constructor(dpSize: DpSize) : this(dpSize.width.value, dpSize.height.value)

  fun toDpSize(): DpSize {
    return DpSize(widthDp.dp, heightDp.dp)
  }
}

@Serializable
private data class WindowZone(
  val encodedValue: String,
) {
  fun toWindowPosition(): WindowPosition {
    val values = encodedValue.split(":")

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

  companion object {
    operator fun invoke(windowPosition: WindowPosition): WindowZone {
      return WindowZone(
        when (windowPosition) {
          is PlatformDefault -> {
            "PlatformDefault"
          }

          is Absolute -> {
            "Absolute:${windowPosition.x.value}:${windowPosition.y.value}"
          }

          is Aligned -> {
            when (val alignment = windowPosition.alignment) {
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
      )
    }
  }
}
