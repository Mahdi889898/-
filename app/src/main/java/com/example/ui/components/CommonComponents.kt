package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.UrgencyLevel
import com.example.ui.theme.MedicalAlertRed
import com.example.ui.theme.MedicalSuccessGreen
import com.example.ui.theme.MedicalWarningOrange

@Composable
fun UrgencyBadge(urgency: UrgencyLevel, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (urgency) {
        UrgencyLevel.ROUTINE -> Triple(
            MedicalSuccessGreen.copy(alpha = 0.15f),
            MedicalSuccessGreen,
            Icons.Default.CheckCircle
        )
        UrgencyLevel.MODERATE -> Triple(
            MedicalWarningOrange.copy(alpha = 0.15f),
            MedicalWarningOrange,
            Icons.Default.Info
        )
        UrgencyLevel.EMERGENCY -> Triple(
            MedicalAlertRed.copy(alpha = 0.15f),
            MedicalAlertRed,
            Icons.Default.Warning
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = " " + urgency.titleFa,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}
