package com.hasbi.util.result

/**
 * Hasbi error hierarchy.
 *
 * Each error has a `userMessage` for UI display (Roman Urdu friendly).
 */
sealed class HasbiError(message: String) : Exception(message) {
    abstract val userMessage: String

    // === Validation errors ===
    data class InvalidTransferError(val reason: String) : HasbiError("Invalid transfer: $reason") {
        override val userMessage: String = when (reason) {
            "same_wallet" -> "Source aur destination wallet alag hone chahiye"
            "negative_amount" -> "Amount 0 se zyada honi chahiye"
            "missing_destination" -> "Destination wallet select karein"
            else -> "Transfer valid nahi hai"
        }
    }

    data class InsufficientBalanceError(val walletName: String) : HasbiError("Insufficient balance in $walletName") {
        override val userMessage: String = "Is wallet mein kafi balance nahi hai"
    }

    // === Not found errors ===
    data class WalletNotFoundError(val id: Long) : HasbiError("Wallet $id not found") {
        override val userMessage: String = "Wallet nahi mila. App refresh karein."
    }

    data class CategoryNotFoundError(val id: Long) : HasbiError("Category $id not found") {
        override val userMessage: String = "Category nahi mili"
    }

    data class TransactionNotFoundError(val id: Long) : HasbiError("Transaction $id not found") {
        override val userMessage: String = "Transaction nahi mili"
    }

    data class DebtNotFoundError(val id: Long) : HasbiError("Debt $id not found") {
        override val userMessage: String = "Udhaar record nahi mila"
    }

    // === Duplicate errors ===
    data class DuplicateWalletError(val name: String) : HasbiError("Wallet '$name' already exists") {
        override val userMessage: String = "Yeh naam ka wallet pehle se hai"
    }

    data class DuplicateCategoryError(val name: String) : HasbiError("Category '$name' already exists in this group") {
        override val userMessage: String = "Yeh naam ki category pehle se hai"
    }

    // === Backup errors ===
    data class BackupEncryptionError(val reason: String) : HasbiError("Backup encryption failed: $reason") {
        override val userMessage: String = "Backup encrypt nahi ho saka. Phir koshish karein."
    }

    data class BackupDecryptionError(val reason: String) : HasbiError("Backup decryption failed: $reason") {
        override val userMessage: String = "Backup decrypt nahi ho saka"
    }

    data object InvalidPassphraseError : HasbiError("Invalid recovery passphrase") {
        override val userMessage: String = "Galat passphrase. Dobara try karein."
    }

    data class SchemaVersionError(val found: Int, val supported: Int) : HasbiError("Schema version $found not supported (max $supported)") {
        override val userMessage: String = "Yeh backup purani version ki hai. Migration required."
    }

    // === Database errors ===
    data class DatabaseError(val reason: String) : HasbiError("Database error: $reason") {
        override val userMessage: String = "Data issue. Phir koshish karein."
    }

    data class DatabaseIntegrityError(val reason: String) : HasbiError("Database integrity: $reason") {
        override val userMessage: String = "Data issue. Customer support se contact karein."
    }

    // === Generic ===
    data class UnknownError(val reason: String) : HasbiError("Unknown error: $reason") {
        override val userMessage: String = "Kuch masla ho gaya. Phir koshish karein."
    }
}
