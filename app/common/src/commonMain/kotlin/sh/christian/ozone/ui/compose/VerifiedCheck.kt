package sh.christian.ozone.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import sh.christian.ozone.model.Profile
import sh.christian.ozone.model.Profile.Verification.NONE
import sh.christian.ozone.model.Profile.Verification.TRUSTED_VERIFIER
import sh.christian.ozone.model.Profile.Verification.VERIFIED

@Composable
fun VerifiedCheck(
  verification: Profile.Verification,
  modifier: Modifier = Modifier,
) {
  when (verification) {
    NONE -> Unit
    VERIFIED -> {
      Icon(
        modifier = modifier
          .size(12.dp)
          .background(MaterialTheme.colorScheme.primary, CircleShape)
          .padding(1.dp),
        painter = rememberVectorPainter(Icons.Default.Check),
        tint = Color.White,
        contentDescription = "Verified",
      )
    }

    TRUSTED_VERIFIER -> {
      Icon(
        modifier = modifier
          .size(12.dp)
          .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
          .padding(1.dp),
        painter = rememberVectorPainter(Icons.Default.Star),
        tint = Color.White,
        contentDescription = "Trusted Verifier",
      )
    }
  }
}
