package com.weartak.plugin.sdk.serverpackage.aidl;

import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12CancelResultParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportStatusParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ReviewResultParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageResultParcel;
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportPayloadParcel;
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportResultParcel;
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageExportStatusParcel;
import com.weartak.plugin.sdk.serverpackage.model.ServerPackageStageRequestParcel;

/**
 * Host-owned typed server package control. Receive side shares the PKCS#12 V1 stage/review
 * lifecycle; export side lets the host choose one configured server and release only
 * non-secret settings. There is intentionally no apply method.
 */
interface IWearTakServerPackageControlV1 {
    Pkcs12StageResultParcel stageServerPackage(in ServerPackageStageRequestParcel request);
    Pkcs12ReviewResultParcel requestReview(String handle);
    Pkcs12ImportStatusParcel getStatus(String handle);
    Pkcs12CancelResultParcel cancel(String handle);
    ServerPackageExportResultParcel requestExport(String requestId);
    ServerPackageExportStatusParcel getExportStatus(String handle);
    ServerPackageExportPayloadParcel takeExport(String handle);
    Pkcs12CancelResultParcel cancelExport(String handle);
}