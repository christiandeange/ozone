package sh.christian.ozone.ui.icons

/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.LocalActivity: ImageVector
  get() {
    if (_localActivity != null) {
      return _localActivity!!
    }
    _localActivity = materialIcon(name = "Filled.LocalActivity") {
      materialPath {
        moveTo(20.0f, 12.0f)
        curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
        lineTo(22.0f, 6.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        lineTo(4.0f, 4.0f)
        curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
        verticalLineToRelative(4.0f)
        curveToRelative(1.1f, 0.0f, 1.99f, 0.9f, 1.99f, 2.0f)
        reflectiveCurveToRelative(-0.89f, 2.0f, -2.0f, 2.0f)
        verticalLineToRelative(4.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(16.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineToRelative(-4.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, -0.9f, -2.0f, -2.0f)
        close()
        moveTo(15.58f, 16.8f)
        lineTo(12.0f, 14.5f)
        lineToRelative(-3.58f, 2.3f)
        lineToRelative(1.08f, -4.12f)
        lineToRelative(-3.29f, -2.69f)
        lineToRelative(4.24f, -0.25f)
        lineTo(12.0f, 5.8f)
        lineToRelative(1.54f, 3.95f)
        lineToRelative(4.24f, 0.25f)
        lineToRelative(-3.29f, 2.69f)
        lineToRelative(1.09f, 4.11f)
        close()
      }
    }
    return _localActivity!!
  }

private var _localActivity: ImageVector? = null
