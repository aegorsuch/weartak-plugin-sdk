package com.weartak.plugin.sdk.model

/**
 * CEP rule stream operation codes for [CepRuleEventParcel].
 *
 * v1.x additive streaming maps these to [com.weartak.plugin.sdk.aidl.IWearTakPluginControl.applyCepRuleEvent]
 * when [com.weartak.plugin.sdk.model.HostCapabilitiesParcel.cepRuleStreaming] is enabled. Hosts without
 * streaming continue to use [com.weartak.plugin.sdk.aidl.IWearTakPluginControl.registerCepRules] /
 * [com.weartak.plugin.sdk.aidl.IWearTakPluginControl.unregisterCepRules] as the compatibility path.
 */
object CepRuleOperation {
    const val ADD: Int = 0
    const val UPDATE: Int = 1
    const val REMOVE: Int = 2
}
