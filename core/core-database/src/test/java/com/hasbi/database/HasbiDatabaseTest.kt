package com.hasbi.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.hasbi.database.entity.CategoryEntity
import com.hasbi.database.entity.CategoryGroup
import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.DebtType
import com.hasbi.database.entity.TransactionEntity
import com.hasbi.database.entity.TransactionType
import com.hasbi.database.entity.WalletEntity
import com.hasbi.database.entity.WalletType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic DAO smoke tests — verify database creates and basic CRUD works.
 *
 * Week 2: Smoke tests only.
 * Week 3+: Comprehensive tests for all financial calculations (transfers, debts, budgets).
 */
@RunWith(AndroidJUnit4::class)
class HasbiDatabaseTest {

    private lateinit var database: HasbiDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            HasbiDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_and_retrieve_currency() = runTest {
        val now = System.currentTimeMillis()
        val currency = CurrencyEntity(
            code = "PKR",
            name = "Pakistani Rupee",
            symbol = "Rs.",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = com.hasbi.database.entity.SymbolPosition.BEFORE,
            isDefault = true,
            createdAt = now
        )

        database.currencyDao().insertAll(listOf(currency))

        val retrieved = database.currencyDao().getByCode("PKR")
        assertNotNull(retrieved)
        assertEquals("Pakistani Rupee", retrieved?.name)
        assertEquals(2, retrieved?.minorUnitExponent)
    }

    @Test
    fun insert_and_retrieve_wallet() = runTest {
        val now = System.currentTimeMillis()
        setupCurrency()

        val wallet = WalletEntity(
            name = "Cash",
            type = WalletType.CASH,
            icon = "💵",
            colorHex = "#1B5E43",
            initialBalance = 10_000_00,  // Rs. 10,000 in paisa
            currencyCode = "PKR",
            sortOrder = 0,
            createdAt = now,
            updatedAt = now
        )

        val walletId = database.walletDao().insert(wallet)
        assertTrue(walletId > 0)

        val retrieved = database.walletDao().getById(walletId)
        assertNotNull(retrieved)
        assertEquals("Cash", retrieved?.name)
        assertEquals(10_000_00, retrieved?.initialBalance)
    }

    @Test
    fun wallet_balance_includes_initial_and_transactions() = runTest {
        val now = System.currentTimeMillis()
        setupCurrency()
        val walletId = setupWallet("Cash", 10_000_00)  // Rs. 10,000

        // Add expense of Rs. 2,500
        database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 2_500_00,
                walletId = walletId,
                categoryId = null,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )

        // Add income of Rs. 5,000
        database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.INCOME,
                amount = 5_000_00,
                walletId = walletId,
                categoryId = null,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )

        val balance = database.walletDao().observeBalance(walletId).first()
        // 10,000 - 2,500 + 5,000 = 12,500
        assertEquals(12_500_00, balance)
    }

    @Test
    fun transfer_does_not_affect_income_or_expense() = runTest {
        val now = System.currentTimeMillis()
        setupCurrency()
        val cashId = setupWallet("Cash", 10_000_00)
        val bankId = setupWallet("Bank", 50_000_00)

        // Transfer Rs. 3,000 from Cash to Bank
        database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.TRANSFER,
                amount = 3_000_00,
                walletId = cashId,
                toWalletId = bankId,
                categoryId = null,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )

        val cashBalance = database.walletDao().observeBalance(cashId).first()
        val bankBalance = database.walletDao().observeBalance(bankId).first()

        // Cash: 10,000 - 3,000 = 7,000
        assertEquals(7_000_00, cashBalance)
        // Bank: 50,000 + 3,000 = 53,000
        assertEquals(53_000_00, bankBalance)

        // Total income should be 0 (transfer is NOT income)
        val totalIncome = database.transactionDao().observeTotalIncome(0, Long.MAX_VALUE).first()
        assertEquals(0L, totalIncome)

        // Total expense should be 0 (transfer is NOT expense)
        val totalExpense = database.transactionDao().observeTotalExpense(0, Long.MAX_VALUE).first()
        assertEquals(0L, totalExpense)
    }

    @Test
    fun soft_delete_and_restore_transaction() = runTest {
        val now = System.currentTimeMillis()
        setupCurrency()
        val walletId = setupWallet("Cash", 10_000_00)

        val txId = database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 500_00,
                walletId = walletId,
                categoryId = null,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )

        // Soft delete
        val purgeTime = now + (30L * 24 * 60 * 60 * 1000)
        database.transactionDao().softDelete(txId, now, purgeTime)

        // Should not be retrievable via getById (excludes deleted)
        assertNull(database.transactionDao().getById(txId))

        // But should be retrievable via getByIdIncludingDeleted
        assertNotNull(database.transactionDao().getByIdIncludingDeleted(txId))

        // Restore
        database.transactionDao().restore(txId, now)

        // Now should be retrievable again
        assertNotNull(database.transactionDao().getById(txId))
    }

    @Test
    fun purge_expired_deletes_only_old_records() = runTest {
        val now = System.currentTimeMillis()
        setupCurrency()
        val walletId = setupWallet("Cash", 10_000_00)

        // Insert tx1 (deleted 31 days ago — should be purged)
        val tx1Id = database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 100_00,
                walletId = walletId,
                categoryId = null,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )
        val oldPurgeTime = now - (31L * 24 * 60 * 60 * 1000)
        database.transactionDao().softDelete(tx1Id, now, oldPurgeTime)

        // Insert tx2 (deleted 5 days ago — should NOT be purged)
        val tx2Id = database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.EXPENSE,
                amount = 200_00,
                walletId = walletId,
                categoryId = null,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )
        val recentPurgeTime = now + (25L * 24 * 60 * 60 * 1000)
        database.transactionDao().softDelete(tx2Id, now, recentPurgeTime)

        // Purge
        val purgedCount = database.transactionDao().purgeExpired(now)
        assertEquals(1, purgedCount)

        // tx1 should be gone
        assertNull(database.transactionDao().getByIdIncludingDeleted(tx1Id))

        // tx2 should still exist (deleted but not purged)
        assertNotNull(database.transactionDao().getByIdIncludingDeleted(tx2Id))
    }

    @Test
    fun debt_remaining_computed_from_linked_transactions() = runTest {
        val now = System.currentTimeMillis()
        setupCurrency()
        val walletId = setupWallet("Cash", 10_000_00)

        // Create LENT debt of Rs. 5,000
        val debtId = database.debtDao().insert(
            DebtEntity(
                type = DebtType.LENT,
                personName = "Ali",
                originalAmount = 5_000_00,
                currencyCode = "PKR",
                startDate = now,
                createdAt = now,
                updatedAt = now
            )
        )

        // Add DEBT_GIVEN transaction (lent money from wallet)
        database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.DEBT_GIVEN,
                amount = 5_000_00,
                walletId = walletId,
                categoryId = null,
                linkedDebtId = debtId,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )

        // Initially remaining = 5,000
        val lentDebts = database.debtDao().observeLentWithRemaining().first()
        assertEquals(1, lentDebts.size)
        assertEquals(5_000_00, lentDebts[0].remainingAmount)

        // Add partial repayment of Rs. 2,000 (DEBT_REPAYMENT_IN)
        database.transactionDao().insert(
            TransactionEntity(
                type = TransactionType.DEBT_REPAYMENT_IN,
                amount = 2_000_00,
                walletId = walletId,
                categoryId = null,
                linkedDebtId = debtId,
                date = now,
                createdAt = now,
                updatedAt = now
            )
        )

        // Now remaining = 5,000 - 2,000 = 3,000
        val updatedDebts = database.debtDao().observeLentWithRemaining().first()
        assertEquals(3_000_00, updatedDebts[0].remainingAmount)
        assertFalse(updatedDebts[0].isSettled)
    }

    @Test
    fun category_keyword_search_finds_correct_category() = runTest {
        val now = System.currentTimeMillis()
        database.categoryDao().insert(
            CategoryEntity(
                name = "Food",
                icon = "🍔",
                colorHex = "#FF9800",
                group = CategoryGroup.EXPENSE,
                sortOrder = 1,
                keywords = "chai,nashta,khana,food,lunch",
                createdAt = now,
                updatedAt = now
            )
        )

        val found = database.categoryDao().findByKeyword("chai", CategoryGroup.EXPENSE)
        assertNotNull(found)
        assertEquals("Food", found?.name)

        val notFound = database.categoryDao().findByKeyword("petrol", CategoryGroup.EXPENSE)
        assertNull(notFound)
    }

    @Test
    fun category_uniqueness_on_name_and_group() = runTest {
        val now = System.currentTimeMillis()

        // Insert "Other" expense
        database.categoryDao().insert(
            CategoryEntity(
                name = "Other",
                icon = "📦",
                colorHex = "#9E9E9E",
                group = CategoryGroup.EXPENSE,
                sortOrder = 99,
                createdAt = now,
                updatedAt = now
            )
        )

        // Insert "Other" income — should succeed (different group)
        database.categoryDao().insert(
            CategoryEntity(
                name = "Other",
                icon = "📦",
                colorHex = "#9E9E9E",
                group = CategoryGroup.INCOME,
                sortOrder = 99,
                createdAt = now,
                updatedAt = now
            )
        )

        // Both should exist
        assertEquals(1, database.categoryDao().countByName("Other", CategoryGroup.EXPENSE))
        assertEquals(1, database.categoryDao().countByName("Other", CategoryGroup.INCOME))
    }

    // === Helpers ===

    private suspend fun setupCurrency() {
        val now = System.currentTimeMillis()
        database.currencyDao().insertAll(
            listOf(
                CurrencyEntity(
                    code = "PKR",
                    name = "Pakistani Rupee",
                    symbol = "Rs.",
                    minorUnitExponent = 2,
                    decimalSeparator = ".",
                    thousandsSeparator = ",",
                    symbolPosition = com.hasbi.database.entity.SymbolPosition.BEFORE,
                    isDefault = true,
                    createdAt = now
                )
            )
        )
    }

    private suspend fun setupWallet(name: String, initialBalance: Long): Long {
        val now = System.currentTimeMillis()
        return database.walletDao().insert(
            WalletEntity(
                name = name,
                type = WalletType.CASH,
                icon = "💵",
                colorHex = "#1B5E43",
                initialBalance = initialBalance,
                currencyCode = "PKR",
                sortOrder = 0,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}

// Helper — JUnit 4 doesn't have assertFalse imported by default in some setups
private fun assertFalse(condition: Boolean) {
    org.junit.Assert.assertFalse(condition)
}
