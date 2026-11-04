package com.weartak.plugin.sdk.nearbyimport.aidl;

import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12CancelResultParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ImportStatusParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12ReviewResultParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageRequestParcel;
import com.weartak.plugin.sdk.nearbyimport.model.Pkcs12StageResultParcel;

/** Host-owned, staging-only PKCS#12 review control. There is intentionally no apply method. */
interface IWearTakPkcs12ImportControlV1 {
    Pkcs12StageResultParcel stagePkcs12(in Pkcs12StageRequestParcel request);
    Pkcs12ReviewResultParcel requestReview(String handle);
    Pkcs12ImportStatusParcel getStatus(String handle);
    Pkcs12CancelResultParcel cancel(String handle);
}
