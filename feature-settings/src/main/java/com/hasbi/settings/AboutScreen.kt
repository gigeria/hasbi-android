package com.hasbi.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.hasbi.design.icons.HasbiArchLogo
import com.hasbi.design.theme.HasbiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val colors = HasbiTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
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
                    text = "About Hasbi",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logo + name (centered)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HasbiArchLogo(size = 100.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "حسبي",
                    color = colors.primary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hasbi",
                    color = colors.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mera Hisab, Mera Yaqeen",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0",
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Story card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Hasbi ki Kahani",
                        color = colors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hasbi ka naam Al-Hasib (الحسيب) se inspired hai — Allah ki sifat jo har cheez ka hisab rakhta hai.\n\nHasbi (حسبي) ka matlab hai 'mere liye kaafee' — ek aap ka saathi jo aap ke paisay ka hisab rakhe, taake aap befikar rahein.\n\nYeh app ek waalid ne apne betay Haseeb ke naam se inspired banayi. Haseeb aur Hasbi, dono ka root H-S-B hai — hisab karna, pukhta karna.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Features",
                        color = colors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureItem("⭐", "Aaj Ka Baqi", "Daily safe spend calculation", colors)
                    FeatureItem("💡", "Smart Entry", "Roman Urdu natural language parser", colors)
                    FeatureItem("🔒", "Privacy First", "No account, data stays on device", colors)
                    FeatureItem("💾", "Encrypted Backup", "Local backup with AES-256", colors)
                    FeatureItem("🤝", "Udhaar Tracking", "Lent & borrowed without double-counting", colors)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.primary, colors.primaryLight)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Aap Ka Data, Aap Ke Paas",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hasbi koi account nahi maangta.\nKoi cloud sync nahi (optional Drive backup Phase 2).\nKoi analytics nahi.\nAap ka data sirf aap ke device pe.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Visit Website button
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://digitalkasur.com"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("🌐 Visit digitalkasur.com", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "App by Ahmad Bin Shafi\nDigital Kasur — digitalkasur.com\n\nMade with ❤️ in Pakistan\nHasbi — Aap ke liye, Haseeb ke liye, sab ke liye.",
                color = colors.textTertiary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureItem(
    icon: String,
    title: String,
    desc: String,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
