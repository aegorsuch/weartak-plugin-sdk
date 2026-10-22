package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ToastRequestParcel(
    val message: String,
    val duration: Int = DURATION_SHORT,
    val tag: String? = null,
) : Parcelable {
    companion object {
        const val DURATION_SHORT: Int = 0
        const val DURATION_LONG: Int = 1
    }
}
