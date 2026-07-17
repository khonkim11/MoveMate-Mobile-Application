package com.example.movemate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * Use this reusable row in ProfileMenuScreen or HealthSectionScreen.
 * Session values remain stored as kg and cm.
 */
@Composable
fun ProfileMeasurementSummary(
    session: UserSession,
    modifier: Modifier = Modifier
) {
    val system =
        LocalMeasurementSystem.current

    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text =
                "Weight: ${
                    formatWeight(
                        session.weightKg,
                        system
                    )
                }",
            modifier =
                Modifier.weight(1f),
            fontWeight =
                FontWeight.SemiBold
        )

        Text(
            text =
                "Height: ${
                    formatHeight(
                        session.heightCm,
                        system
                    )
                }",
            modifier =
                Modifier.weight(1f),
            fontWeight =
                FontWeight.SemiBold
        )
    }
}
