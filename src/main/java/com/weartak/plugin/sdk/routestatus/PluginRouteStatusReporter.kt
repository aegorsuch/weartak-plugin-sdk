package com.weartak.plugin.sdk.routestatus

import android.os.IBinder
import com.weartak.plugin.sdk.model.PluginInitParcel
import com.weartak.plugin.sdk.routestatus.aidl.IPluginRouteStatusControlV1
import com.weartak.plugin.sdk.routestatus.model.PluginRouteStatusParcel
import com.weartak.plugin.sdk.validation.ContractValidationResult
import java.util.concurrent.atomic.AtomicLong

/**
 * Plugin-side client for [IPluginRouteStatusControlV1]. Obtain via [fromInit]; null means the
 * host does not support route status. Make calls off the main thread. Transport failures return
 * [RouteStatusResultCode.UNAVAILABLE], never [RouteStatusResultCode.OK].
 */
class PluginRouteStatusReporter internal constructor(
    private val control: IPluginRouteStatusControlV1,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val sequence = AtomicLong(0L)

    fun report(routeId: String, label: String, state: Int, statusText: String? = null): Int {
        val status = PluginRouteStatusParcel(
            routeId = routeId,
            label = label,
            state = state,
            statusText = statusText,
            sequence = sequence.incrementAndGet(),
            changedAtEpochMillis = clock(),
        )
        val valid = when (val result = RouteStatusValidator.validate(status)) {
            is ContractValidationResult.Ok -> result.value
            is ContractValidationResult.Err -> return RouteStatusResultCode.INVALID_REQUEST
        }
        return call { control.reportRouteStatus(valid) }
    }

    fun remove(routeId: String): Int {
        if (RouteStatusValidator.validateRouteId(routeId) is ContractValidationResult.Err) {
            return RouteStatusResultCode.INVALID_REQUEST
        }
        return call { control.removeRoute(routeId) }
    }

    fun clear(): Int = call { control.clearRoutes() }

    private inline fun call(block: () -> Int): Int = try {
        block()
    } catch (_: Exception) {
        RouteStatusResultCode.UNAVAILABLE
    }

    companion object {
        /** Requires both the capability flag and a Binder in [PluginInitParcel.hostExtras]. */
        fun fromInit(init: PluginInitParcel?): PluginRouteStatusReporter? {
            val extras = init?.hostExtras ?: return null
            if (!extras.getBoolean(RouteStatusContract.EXTRA_CAPABILITY, false)) return null
            val binder: IBinder = extras.getBinder(RouteStatusContract.EXTRA_CONTROL) ?: return null
            if (!binder.isBinderAlive) return null
            return PluginRouteStatusReporter(IPluginRouteStatusControlV1.Stub.asInterface(binder))
        }
    }
}
