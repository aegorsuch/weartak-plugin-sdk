package com.weartak.plugin.sdk.v2.binder

import com.weartak.plugin.sdk.v2.aidl.IWearTakPluginHostControlV2
import com.weartak.plugin.sdk.v2.model.CepRuleStreamEventParcel
import com.weartak.plugin.sdk.v2.model.DetectionEventParcel
import com.weartak.plugin.sdk.v2.model.HostCapabilitiesV2Parcel
import com.weartak.plugin.sdk.v2.model.TaskCommandCompletionParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandProgressParcel
import com.weartak.plugin.sdk.v2.model.TaskCommandSubmitParcel
import com.weartak.plugin.sdk.v2.model.V2HostResultParcel

/** Test double for [IWearTakPluginHostControlV2] with synchronous accept-by-default semantics. */
open class V2RecordingHostControl(
    private val caps: HostCapabilitiesV2Parcel,
    private val defaultAccepted: Boolean = true,
) : IWearTakPluginHostControlV2.Stub() {

    val cepEvents = mutableListOf<CepRuleStreamEventParcel>()
    val detections = mutableListOf<DetectionEventParcel>()
    val submissions = mutableListOf<TaskCommandSubmitParcel>()
    val progressReports = mutableListOf<TaskCommandProgressParcel>()
    val completions = mutableListOf<TaskCommandCompletionParcel>()

    override fun getHostCapabilities(): HostCapabilitiesV2Parcel = caps

    override fun applyCepRuleEvent(event: CepRuleStreamEventParcel?): V2HostResultParcel {
        if (event != null) cepEvents += event
        return V2HostResultParcel(accepted = defaultAccepted)
    }

    override fun publishDetectionEvent(event: DetectionEventParcel?): V2HostResultParcel {
        if (event != null) detections += event
        return V2HostResultParcel(accepted = defaultAccepted)
    }

    override fun submitTaskCommand(request: TaskCommandSubmitParcel?): V2HostResultParcel {
        if (request != null) submissions += request
        return V2HostResultParcel(accepted = defaultAccepted)
    }

    override fun reportTaskCommandProgress(progress: TaskCommandProgressParcel?): V2HostResultParcel {
        if (progress != null) progressReports += progress
        return V2HostResultParcel(accepted = defaultAccepted)
    }

    override fun reportTaskCommandCompletion(completion: TaskCommandCompletionParcel?): V2HostResultParcel {
        if (completion != null) completions += completion
        return V2HostResultParcel(accepted = defaultAccepted)
    }
}
