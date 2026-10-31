package com.weartak.plugin.sdk.routestatus

import com.weartak.plugin.sdk.routestatus.model.PluginRouteStatusParcel
import com.weartak.plugin.sdk.validation.ContractIssue
import com.weartak.plugin.sdk.validation.ContractValidationResult
import com.weartak.plugin.sdk.validation.PluginStringPolicy

/**
 * Shared plugin/host validation. Hard-fails shape violations; truncates text; maps
 * unrecognized future states to [RouteConnectionState.UNKNOWN].
 */
object RouteStatusValidator {
    fun validateRouteId(routeId: String?): ContractValidationResult<String> =
        if (routeId != null && RouteStatusContract.ROUTE_ID_PATTERN.matches(routeId)) {
            ContractValidationResult.Ok(routeId)
        } else {
            ContractValidationResult.Err(ContractIssue("route_id_invalid", "routeId must match [A-Za-z0-9._-]{1,64}"))
        }

    fun validate(status: PluginRouteStatusParcel?): ContractValidationResult<PluginRouteStatusParcel> {
        if (status == null) return ContractValidationResult.Err(ContractIssue("route_status_null", "status is null"))
        val issues = mutableListOf<ContractIssue>()
        (validateRouteId(status.routeId) as? ContractValidationResult.Err)?.let { issues += it.issues }
        val label = printable(status.label, RouteStatusContract.MAX_LABEL_BYTES)?.trim()
        if (label.isNullOrEmpty()) issues += ContractIssue("route_label_invalid", "label must be nonblank printable text")
        val text = status.statusText?.let { printable(it, RouteStatusContract.MAX_STATUS_TEXT_BYTES) }
        if (status.statusText != null && text == null) {
            issues += ContractIssue("route_status_text_invalid", "statusText must be printable text")
        }
        if (status.sequence < 0) issues += ContractIssue("route_sequence_negative", "sequence must be nonnegative")
        if (status.changedAtEpochMillis < 0) issues += ContractIssue("route_time_negative", "changedAtEpochMillis must be nonnegative")
        if (issues.isNotEmpty()) return ContractValidationResult.Err(issues)
        return ContractValidationResult.Ok(
            status.copy(
                label = label!!,
                state = if (RouteConnectionState.isKnown(status.state)) status.state else RouteConnectionState.UNKNOWN,
                statusText = text?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    private fun printable(input: String, maxBytes: Int): String? =
        PluginStringPolicy.requireUtf8Printable(input.replace('\n', ' ').replace('\t', ' '), maxBytes).getOrNull()
}
