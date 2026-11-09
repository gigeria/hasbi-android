package com.hasbi.settings.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.hasbi.database.entity.ProjectEntity
import com.hasbi.design.theme.HasbiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val projects by viewModel.projects.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val error by viewModel.error.collectAsState()
    val colors = HasbiTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
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
                    text = "Projects",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = viewModel::showAddDialog) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = colors.primary
                    )
                }
            }

            Text(
                text = "Apne projects khud banayein.\nHar transaction par project tag lagayein.\nReports project-wise niklein.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(projects) { project ->
                    ProjectRow(
                        project = project,
                        onArchive = { viewModel.archiveProject(project.id) },
                        colors = colors
                    )
                }
            }
        }

        error?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = colors.expense,
                contentColor = Color.White
            ) { Text(msg) }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = viewModel::hideAddDialog,
            onAdd = viewModel::addProject,
            colors = colors
        )
    }
}

@Composable
private fun ProjectRow(
    project: ProjectEntity,
    onArchive: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.backgroundWarm),
            contentAlignment = Alignment.Center
        ) {
            Text(text = project.icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = project.name,
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onArchive) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove",
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🏠") }
    var selectedColor by remember { mutableStateOf("#1B5E43") }

    val icons = listOf("🏠", "💼", "🏪", "🎓", "🚗", "📱", "🎨", "⚽", "✈️", "🌱", "📦", "💻")
    val colorOptions = listOf("#1B5E43", "#2196F3", "#E91E63", "#FF9800", "#9C27B0", "#607D8B", "#4CAF80", "#F44336")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = { Text("New Project", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name (e.g. Dukaan, School)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Icon", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    icons.take(6).forEach { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.backgroundWarm)
                                .border(if (isSelected) 2.dp else 0.dp, if (isSelected) colors.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) { Text(text = icon, fontSize = 16.sp) }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    icons.drop(6).forEach { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.backgroundWarm)
                                .border(if (isSelected) 2.dp else 0.dp, if (isSelected) colors.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) { Text(text = icon, fontSize = 16.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Color", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { colorHex ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseColor(colorHex))
                                .border(if (isSelected) 3.dp else 0.dp, colors.primaryGold, CircleShape)
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name.trim(), selectedIcon, selectedColor) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
            ) { Text("Add Project") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) }
        }
    )
}

private fun parseColor(hex: String): Color {
    return try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
}
