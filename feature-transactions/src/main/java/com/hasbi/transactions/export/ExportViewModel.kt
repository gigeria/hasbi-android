package com.hasbi.transactions.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    fun exportToCSV(uri: Uri, startDate: Long, endDate: Long) {
        _state.update { it.copy(isExporting = true, error = null) }
        viewModelScope.launch {
            try {
                val transactions = collectTransactions(startDate, endDate)
                val csv = buildCSV(transactions)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csv.toByteArray())
                }
                _state.update {
                    it.copy(
                        isExporting = false,
                        message = "Exported ${transactions.size} transactions to CSV!"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, error = "Export failed: ${e.message}") }
            }
        }
    }

    fun exportToPDF(uri: Uri, startDate: Long, endDate: Long) {
        _state.update { it.copy(isExporting = true, error = null) }
        viewModelScope.launch {
            try {
                val transactions = collectTransactions(startDate, endDate)
                val pdf = buildSimplePDF(transactions)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(pdf.toByteArray())
                }
                _state.update {
                    it.copy(
                        isExporting = false,
                        message = "Exported ${transactions.size} transactions to PDF!"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, error = "Export failed: ${e.message}") }
            }
        }
    }

    private suspend fun collectTransactions(startDate: Long, endDate: Long): List<Transaction> {
        val txs = mutableListOf<Transaction>()
        transactionRepository.observeByDateRange(startDate, endDate).collect { list ->
            txs.clear()
            txs.addAll(list)
            return@collect
        }
        return txs
    }

    private fun buildCSV(transactions: List<Transaction>): String {
        val sb = StringBuilder()
        sb.append("Date,Type,Amount (Rs),Wallet,Category,Note\n")

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val wallets = mutableMapOf<Long, String>()

        transactions.forEach { tx ->
            val date = sdf.format(Date(tx.date))
            val type = tx.type.name
            val amount = tx.amount / 100
            val walletName = wallets.getOrPut(tx.walletId) { "Wallet" }
            val category = "Category"
            val note = tx.note?.replace(",", ";")?.replace("\n", " ") ?: ""
            sb.append("$date,$type,$amount,$walletName,$category,$note\n")
        }

        // Summary
        sb.append("\n\nSummary\n")
        sb.append("Total Income,${transactions.filter { it.type == com.hasbi.database.entity.TransactionType.INCOME }.sumOf { it.amount } / 100}\n")
        sb.append("Total Expense,${transactions.filter { it.type == com.hasbi.database.entity.TransactionType.EXPENSE }.sumOf { it.amount } / 100}\n")
        val net = transactions.filter { it.type == com.hasbi.database.entity.TransactionType.INCOME }.sumOf { it.amount } -
                  transactions.filter { it.type == com.hasbi.database.entity.TransactionType.EXPENSE }.sumOf { it.amount }
        sb.append("Net,${net / 100}\n")

        return sb.toString()
    }

    private fun buildSimplePDF(transactions: List<Transaction>): String {
        // Phase 1: simple text-based "PDF" (actually HTML that can be printed)
        // Phase 1.1: use proper PDF library
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        sb.append("HASBI — Transaction Report\n")
        sb.append("Generated: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}\n")
        sb.append("=" .repeat(50)).append("\n\n")

        sb.append("Total Transactions: ${transactions.size}\n")

        val totalIncome = transactions.filter { it.type == com.hasbi.database.entity.TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == com.hasbi.database.entity.TransactionType.EXPENSE }.sumOf { it.amount }

        sb.append("Total Income: Rs. ${totalIncome / 100}\n")
        sb.append("Total Expense: Rs. ${totalExpense / 100}\n")
        sb.append("Net: Rs. ${(totalIncome - totalExpense) / 100}\n\n")

        sb.append("=" .repeat(50)).append("\n")
        sb.append("TRANSACTIONS\n")
        sb.append("=" .repeat(50)).append("\n\n")

        transactions.forEach { tx ->
            val date = sdf.format(Date(tx.date))
            val type = tx.type.name
            val amount = tx.amount / 100
            val note = tx.note ?: ""
            sb.append("$date | $type | Rs. $amount | $note\n")
        }

        return sb.toString()
    }

    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }
}

data class ExportState(
    val isExporting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
