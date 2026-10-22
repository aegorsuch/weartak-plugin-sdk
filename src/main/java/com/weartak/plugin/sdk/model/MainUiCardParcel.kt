package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

object MainUiCardTypes {
    const val STATUS: String = "STATUS"
    const val KEY_VALUE_LIST: String = "KEY_VALUE_LIST"
}

/**
 * Declarative main-UI card. Host v1 must support [MainUiCardTypes.STATUS] and [MainUiCardTypes.KEY_VALUE_LIST].
 */
@Parcelize
data class MainUiCardParcel(
    val cardType: String,
    val cardId: String,
    val title: String,
    val subtitle: String? = null,
    val content: Bundle = Bundle(),
) : Parcelable
