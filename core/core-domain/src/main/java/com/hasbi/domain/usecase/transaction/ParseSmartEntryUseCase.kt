package com.hasbi.domain.usecase.transaction

import com.hasbi.database.entity.CategoryGroup
import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Category
import com.hasbi.domain.model.SmartEntryResult
import com.hasbi.domain.model.Wallet
import com.hasbi.domain.repository.CategoryRepository
import com.hasbi.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Parse natural language input for Smart Entry.
 *
 * Phase 1: Local rules + curated keywords. NO cloud AI.
 *
 * Examples:
 *   "500 chai"          → EXPENSE, Rs. 500, Food (keyword "chai")
 *   "2500 petrol bank"  → EXPENSE, Rs. 2500, Transport (keyword "petrol"), Bank wallet
 *   "sabzi 500"         → EXPENSE, Rs. 500, Grocery (keyword "sabzi")
 *   "salary 50000"      → INCOME, Rs. 50000, Salary (keyword "salary")
 *
 * Confidence scoring:
 *   - amount found: +0.4
 *   - category matched: +0.4
 *   - wallet matched: +0.2
 *
 * Future (Phase 2+): on-device ML model for better matching.
 */
class ParseSmartEntryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) {

    private val amountRegex = Regex("""(\d[\d,]*)\s*(?:rs|rupees|pkr|₹)?""", RegexOption.IGNORE_CASE)
    private val incomeKeywords = listOf("salary", "tankhuwa", "payment received", "income", "received", "freelance", "bonus", "profit")

    suspend operator fun invoke(text: String): SmartEntryResult {
        if (text.isBlank()) return SmartEntryResult.empty(text)

        // 1. Extract amount
        val amountMatch = amountRegex.find(text)
        val amount = amountMatch?.value
            ?.replace(",", "")
            ?.replace(Regex("(?i)rs|rupees|pkr|₹"), "")
            ?.trim()
            ?.toLongOrNull() ?: 0L

        // 2. Clean text (remove amount for keyword search)
        val cleanedText = amountMatch?.let {
            text.replace(it.value, "").lowercase().trim()
        } ?: text.lowercase().trim()

        // Split into words for keyword matching
        val words = cleanedText.split(Regex("\\s+")).filter { it.isNotBlank() }

        // 3. Detect type (income vs expense)
        val isIncome = incomeKeywords.any { keyword ->
            words.any { word -> word.contains(keyword) || keyword.contains(word) }
        }
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
        val group = if (isIncome) CategoryGroup.INCOME else CategoryGroup.EXPENSE

        // 4. Find matching category by keyword
        var matchedCategory: Category? = null
        for (word in words) {
            val found = categoryRepository.findByKeyword(word, group)
            if (found != null) {
                matchedCategory = found
                break
            }
        }

        // 5. Find matching wallet by name
        val wallets = walletRepository.observeVisible().let { flow ->
            var list: List<Wallet> = emptyList()
            flow.collect { list = it; return@collect }
            list
        }
        val matchedWallet = wallets.firstOrNull { wallet ->
            words.any { word -> wallet.name.lowercase().contains(word) }
        } ?: wallets.firstOrNull { it.type == com.hasbi.database.entity.WalletType.CASH }

        // 6. Compute confidence
        var confidence = 0f
        if (amount > 0) confidence += 0.4f
        if (matchedCategory != null) confidence += 0.4f
        if (matchedWallet != null) confidence += 0.2f

        return SmartEntryResult(
            amount = amount,
            category = matchedCategory,
            wallet = matchedWallet,
            type = type,
            confidence = confidence,
            originalText = text
        )
    }
}
