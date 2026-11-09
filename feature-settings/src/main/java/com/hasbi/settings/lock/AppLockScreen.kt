package com.hasbi.settings.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.design.theme.HasbiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = HasbiTheme.colors

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = colors.textPrimary)
            }
            Text("App Lock", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(40.dp))

        // Lock icon
        Text("🔒", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))

        Text(
            "Apna Hisab, Apna Tala",
            color = colors.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "App ko lock karne ke liye 4-digit PIN set karein.\nApp har baar khulne par PIN maangega.",
            color = colors.textSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(32.dp))

        // PIN dots
        Text("PIN (4 digits)", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) { index ->
                val isFilled = index < state.enteredPin.length
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) colors.primary else colors.backgroundWarm)
                        .border(2.dp, if (isFilled) colors.primary else colors.border, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // PIN input
        OutlinedTextField(
            value = state.enteredPin,
            onValueChange = viewModel::setPin,
            placeholder = { Text("••••", color = colors.textTertiary, textAlign = TextAlign.Center) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        // Confirm PIN
        Text("Confirm PIN", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.confirmPin,
            onValueChange = viewModel::setConfirmPin,
            placeholder = { Text("••••", color = colors.textTertiary, textAlign = TextAlign.Center) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )

        // Error
        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(error, color = colors.expense, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))

        // Save button
        Button(
            onClick = viewModel::savePin,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = state.enteredPin.length == 4 && state.confirmPin.length == 4,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Set PIN Lock", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "⚠️ Phase 1: Basic PIN storage. Phase 1.1 mein encrypted + biometric support aayega.",
            color = colors.textTertiary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}
