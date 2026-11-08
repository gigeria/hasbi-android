package com.hasbi.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.design.icons.HasbiArchLogo
import com.hasbi.design.theme.HasbiTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = HasbiTheme.colors

    // File picker for backup (create)
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.createBackup(it) }
    }

    // File picker for restore (open)
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreBackup(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "Backup & Restore",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero card (gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.primary, colors.primaryLight)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HasbiArchLogo(size = 60.dp, showArabic = false)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Encrypted Backup",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aap ka data locally save hota hai.\nAES-256 encryption (Phase 1.1)",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create Backup button
            Button(
                onClick = {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
                    createBackupLauncher.launch("hasbi_backup_$timestamp.json")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isBackingUp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isBackingUp) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("📤 Create Backup", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restore button
            OutlinedButton(
                onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isRestoring,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isRestoring) {
                    CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(20.dp))
                } else {
                    Text("📥 Restore from File", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            InfoCard(colors = colors)

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun InfoCard(colors: com.hasbi.design.theme.HasbiColorPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoItem(
            icon = "🔐",
            title = "Local & Encrypted",
            desc = "Backup file sirf aap ke device pe save hoti hai",
            colors = colors
        )
        InfoItem(
            icon = "☁️",
            title = "Google Drive (Phase 2)",
            desc = "Cloud backup aane wala hai",
            colors = colors
        )
        InfoItem(
            icon = "🔑",
            title = "Recovery Passphrase (Phase 1.1)",
            desc = "Naye device pe restore karne ke liye",
            colors = colors
        )
    }
}

@Composable
private fun InfoItem(
    icon: String,
    title: String,
    desc: String,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}
