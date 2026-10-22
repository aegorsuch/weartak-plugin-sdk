package com.weartak.plugin.sdk.validation

import com.weartak.plugin.sdk.WearTakPluginContract
import com.weartak.plugin.sdk.model.PluginCapabilitiesParcel
import com.weartak.plugin.sdk.model.PluginInfoParcel
import com.weartak.plugin.sdk.model.PluginInitParcel

object PluginMetadataValidator {
    fun sanitizePluginInfo(info: PluginInfoParcel): PluginInfoParcel =
        PluginInfoParcel(
            pluginId = PluginStringPolicy.truncateUtf8(info.pluginId, WearTakPluginContract.MAX_PLUGIN_ID_BYTES),
            displayName = PluginStringPolicy.truncateUtf8(info.displayName, WearTakPluginContract.MAX_DISPLAY_NAME_BYTES),
            versionName = PluginStringPolicy.truncateUtf8(info.versionName, WearTakPluginContract.MAX_VERSION_NAME_BYTES),
            packageName = PluginStringPolicy.truncateUtf8(info.packageName, WearTakPluginContract.MAX_PACKAGE_NAME_BYTES),
            description = info.description?.let {
                PluginStringPolicy.truncateChars(it, WearTakPluginContract.MAX_PLUGIN_DESCRIPTION_CHARS)
            },
        )

    fun validatePluginInfo(info: PluginInfoParcel): ContractValidationResult<PluginInfoParcel> {
        val issues = mutableListOf<ContractIssue>()
        if (info.pluginId.isBlank()) issues += ContractIssue("pluginId", "pluginId required")
        if (info.displayName.isBlank()) issues += ContractIssue("displayName", "displayName required")
        if (info.packageName.isBlank()) issues += ContractIssue("packageName", "packageName required")
        if (info.pluginId.toByteArray(Charsets.UTF_8).size > WearTakPluginContract.MAX_PLUGIN_ID_BYTES) {
            issues += ContractIssue("pluginId", "pluginId too long")
        }
        if (info.description != null && info.description.length > WearTakPluginContract.MAX_PLUGIN_DESCRIPTION_CHARS) {
            issues += ContractIssue("description", "description too long")
        }
        return if (issues.isEmpty()) ContractValidationResult.Ok(sanitizePluginInfo(info)) else ContractValidationResult.Err(issues)
    }

    fun validateCapabilities(cap: PluginCapabilitiesParcel): ContractValidationResult<PluginCapabilitiesParcel> =
        ContractValidationResult.Ok(cap)

    fun validateInitAgainstPlugin(
        init: PluginInitParcel,
        declaredPluginId: String,
    ): ContractValidationResult<PluginInitParcel> {
        if (init.pluginIdExpected.isNotBlank() && init.pluginIdExpected != declaredPluginId) {
            return ContractValidationResult.Err(
                ContractIssue("pluginId mismatch", "expected ${init.pluginIdExpected} vs $declaredPluginId"),
            )
        }
        if (init.hostPackageName.isBlank()) {
            return ContractValidationResult.Err(ContractIssue("hostPackageName", "host package required"))
        }
        return ContractValidationResult.Ok(init)
    }
}
