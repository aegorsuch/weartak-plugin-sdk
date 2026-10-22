package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.model.SettingsValidationErrorParcel

internal fun List<ContractIssue>.toValidationErrors(): List<SettingsValidationErrorParcel> =
    map { issue ->
        SettingsValidationErrorParcel(
            fieldId = issue.code.substringAfterLast('.').takeIf { it.isNotBlank() } ?: "",
            code = issue.code,
            message = issue.message,
        )
    }
