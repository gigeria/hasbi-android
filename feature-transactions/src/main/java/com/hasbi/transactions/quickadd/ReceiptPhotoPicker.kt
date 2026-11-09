package com.hasbi.transactions.quickadd

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.design.theme.HasbiTheme

/**
 * Receipt photo picker — lets user attach a photo to transaction.
 *
 * Uses Android Photo Picker (no permission needed).
 */
@Composable
fun ReceiptPhotoPicker(
    photoUri: String?,
    onPhotoSelected: (String) -> Unit
) {
    val colors = HasbiTheme.colors
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoSelected(it.toString()) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundWarm)
            .clickable { launcher.launch("image/*") }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "📷", fontSize = 18.sp)
            Text(
                text = if (photoUri != null) "Receipt attached ✓ (tap to change)" else "Attach receipt photo (optional)",
                color = if (photoUri != null) colors.income else colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
