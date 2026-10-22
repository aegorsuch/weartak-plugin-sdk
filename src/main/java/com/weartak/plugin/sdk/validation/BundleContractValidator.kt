package com.weartak.plugin.sdk.validation

import android.os.Bundle
import com.weartak.plugin.sdk.PluginLimits
import com.weartak.plugin.sdk.WearTakPluginContract

/**
 * Bounds checks for [Bundle] payloads crossing the plugin contract (sensor values, CoT extras, markers).
 */
object BundleContractValidator {
    fun validateContentBundle(bundle: Bundle): ContractValidationResult<Bundle> {
        val keys = bundle.keySet()
        if (keys.size > WearTakPluginContract.MAX_BUNDLE_KEYS) {
            return ContractValidationResult.Err(
                ContractIssue("bundle.keys", "Too many keys: ${keys.size}"),
            )
        }
        for (key in keys) {
            if (key.length > PluginLimits.MAX_KEY_LENGTH) {
                return ContractValidationResult.Err(ContractIssue("bundle.key", "Key too long"))
            }
            val s = bundle.getString(key) ?: continue
            val bytes = s.toByteArray(Charsets.UTF_8)
            if (bytes.size > WearTakPluginContract.MAX_BUNDLE_STRING_VALUE_BYTES) {
                return ContractValidationResult.Err(
                    ContractIssue("bundle.value", "String value for $key exceeds max bytes"),
                )
            }
        }
        return ContractValidationResult.Ok(bundle)
    }
}
