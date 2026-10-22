package com.weartak.plugin.sdk.validation

/**
 * Result of contract validation / sanitization. Prefer returning sanitized successes
 * for strings (truncate) and hard failures for shape violations.
 */
sealed class ContractValidationResult<out T> {
    data class Ok<T>(val value: T) : ContractValidationResult<T>()
    data class Err(val issues: List<ContractIssue>) : ContractValidationResult<Nothing>() {
        constructor(single: ContractIssue) : this(listOf(single))
    }

    fun toResult(): Result<T> = when (this) {
        is Ok -> Result.success(value)
        is Err -> Result.failure(IllegalArgumentException(issues.joinToString { it.message }))
    }
}

data class ContractIssue(
    val code: String,
    val message: String,
)
