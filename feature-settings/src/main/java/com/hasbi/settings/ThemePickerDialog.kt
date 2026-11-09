package com.hasbi.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.repository.ThemeMode

@Composable
fun ThemePickerDialog(
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = HasbiTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("Theme", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ThemeOption("System Default", "Phone setting follow karega", ThemeMode.SYSTEM, currentMode, onSelect, colors)
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption("Light", "Hamesha light theme", ThemeMode.LIGHT, currentMode, onSelect, colors)
                Spacer(modifier = Modifier.height(8.dp))
                ThemeOption("Dark", "Hamesha dark theme", ThemeMode.DARK, currentMode, onSelect, colors)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = colors.primary) }
        }
    )
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    mode: ThemeMode,
    currentMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val isSelected = mode == currentMode
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.backgroundWarm)
            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) colors.primary else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onSelect(mode); }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(mode) },
            colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = colors.textSecondary, fontSize = 11.sp)
        }
    }
}
