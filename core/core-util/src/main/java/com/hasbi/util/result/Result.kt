package com.hasbi.util.result

/**
 * Hasbi Result wrapper — replaces try/catch with explicit error handling.
 *
 * Usage in Use Cases:
 * ```
 * suspend fun createWallet(wallet: Wallet): Result<Long> = try {
 *     val id = walletDao.insert(wallet.toEntity())
 *     Result.Success(id)
 * } catch (e: Exception) {
 *     Result.Failure(HasbiError.DatabaseError(e.message ?: "Unknown error"))
 * }
 * ```
 *
 * Usage in ViewModels:
 * ```
 * when (val result = createWalletUseCase(wallet)) {
 *     is Result.Success -> showSuccess()
 *     is Result.Failure -> showError(result.error.userMessage)
 * }
 * ```
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: HasbiError) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (HasbiError) -> Unit): Result<T> {
        if (this is Failure) action(error)
        return this
    }

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): HasbiError? = (this as? Failure)?.error
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}
