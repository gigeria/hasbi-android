package com.hasbi.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.util.date.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val zone = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
    private val today = LocalDate.now(zone)
    private val monthStart = DateTimeUtils.startOfMonth(today.year, today.monthValue, zone)
    private val monthEnd = DateTimeUtils.endOfMonth(today.year, today.monthValue, zone)

    val totalIncome: StateFlow<Long> = transactionRepository.observeTotalIncome(monthStart, monthEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalExpense: StateFlow<Long> = transactionRepository.observeTotalExpense(monthStart, monthEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onExportClick: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val colors = HasbiTheme.colors
    val net = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
            Text("Reports", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Month summary card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .padding(20.dp)
        ) {
            Column {
                Text("This Month", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))

                // Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📈 Income", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Rs. ${totalIncome / 100}", color = colors.income, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = colors.borderSoft)
                Spacer(modifier = Modifier.height(8.dp))

                // Expense
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📉 Expense", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Rs. ${totalExpense / 100}", color = colors.expense, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = colors.borderSoft)
                Spacer(modifier = Modifier.height(8.dp))

                // Net
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("💰 Net", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Rs. ${net / 100}",
                        color = if (net >= 0) colors.income else colors.expense,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export button
        Button(
            onClick = onExportClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("📊 Export Detailed Report", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tips card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.backgroundWarm)
                .padding(16.dp)
        ) {
            Column {
                Text("💡 Coming in Phase 2", color = colors.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Category-wise breakdown charts", color = colors.textSecondary, fontSize = 12.sp)
                Text("• Month vs month comparison", color = colors.textSecondary, fontSize = 12.sp)
                Text("• Project-wise reports", color = colors.textSecondary, fontSize = 12.sp)
                Text("• Spending trends (weekly/monthly)", color = colors.textSecondary, fontSize = 12.sp)
            }
        }
    }
}
