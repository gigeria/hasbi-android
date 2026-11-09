package com.hasbi.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.repository.AppLanguage
import com.hasbi.domain.repository.BackupFrequency
import com.hasbi.domain.repository.NumberFormatStyle
import com.hasbi.domain.repository.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onCategoriesClick: () -> Unit,
    onBackupClick: () -> Unit,
    onAboutClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val colors = HasbiTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Text(
                text = "Settings",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // === Preferences ===
        SectionTitle("Preferences", colors)
        SettingsGroup(colors) {
            SettingsRow(
                icon = "🎨",
                title = "Theme",
                value = settings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { /* Show theme picker */ },
                colors = colors
            )
            SettingsRow(
                icon = "🌐",
                title = "Language",
                value = when (settings.language) {
                    AppLanguage.ENGLISH -> "English"
                    AppLanguage.ROMAN_URDU -> "Roman Urdu"
                    AppLanguage.URDU -> "Urdu"
                },
                onClick = { /* Show language picker */ },
                colors = colors
            )
            SettingsRow(
                icon = "🔢",
                title = "Number Format",
                value = when (settings.numberFormat) {
                    NumberFormatStyle.WESTERN -> "1,000,000"
                    NumberFormatStyle.PAKISTANI -> "10,00,000"
                    NumberFormatStyle.INDIAN -> "10,00,000"
                },
                onClick = { },
                colors = colors
            )
            ToggleRow(
                icon = "👁",
                title = "Hide Balance on Launch",
                isOn = settings.hideBalanceOnLaunch,
                onToggle = viewModel::setHideBalanceOnLaunch,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Features ===
        SectionTitle("Features", colors)
        SettingsGroup(colors) {
            ToggleRow(
                icon = "💡",
                title = "Smart Entry",
                isOn = settings.smartEntryEnabled,
                onToggle = viewModel::setSmartEntry,
                colors = colors
            )
            ToggleRow(
                icon = "🔔",
                title = "Weekly Notification",
                isOn = settings.weeklyNotificationEnabled,
                onToggle = viewModel::setWeeklyNotification,
                colors = colors
            )
            ToggleRow(
                icon = "🔥",
                title = "Streaks",
                isOn = settings.streaksEnabled,
                onToggle = viewModel::setStreaks,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Security ===
        SectionTitle("Security", colors)
        SettingsGroup(colors) {
            ToggleRow(
                icon = "🔒",
                title = "Biometric Lock",
                isOn = settings.biometricLockEnabled,
                onToggle = viewModel::setBiometricLock,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === Data ===
        SectionTitle("Data & Backup", colors)
        SettingsGroup(colors) {
            SettingsRow(
                icon = "🏷️",
                title = "Categories",
                value = "Manage",
                onClick = onCategoriesClick,
                colors = colors
            )
            SettingsRow(
                icon = "💾",
                title = "Backup & Restore",
                value = "Encrypted",
                onClick = onBackupClick,
                colors = colors
            )
            ToggleRow(
                icon = "🔄",
                title = "Auto Weekly Backup",
                isOn = settings.autoLocalBackup,
                onToggle = viewModel::setAutoLocalBackup,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === About ===
        SectionTitle("About", colors)
        SettingsGroup(colors) {
            SettingsRow(
                icon = "ℹ️",
                title = "About Hasbi",
                value = "v1.0.0",
                onClick = onAboutClick,
                colors = colors
            )
            SettingsRow(
                icon = "❤️",
                title = "Mera Hisab, Mera Yaqeen",
                value = "",
                onClick = { },
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SectionTitle(text: String, colors: com.hasbi.design.theme.HasbiColorPalette) {
    Text(
        text = text,
        color = colors.textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsGroup(
    colors: com.hasbi.design.theme.HasbiColorPalette,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface),
        content = content
    )
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    value: String,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                color = colors.textTertiary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = "›",
            color = colors.textTertiary,
            fontSize = 16.sp
        )
    }
    HorizontalDivider(color = colors.borderSoft, thickness = 0.5.dp)
}

@Composable
private fun ToggleRow(
    icon: String,
    title: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.border
            )
        )
    }
    HorizontalDivider(color = colors.borderSoft, thickness = 0.5.dp)
}
