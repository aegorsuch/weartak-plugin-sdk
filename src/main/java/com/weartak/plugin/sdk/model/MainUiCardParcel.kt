package com.weartak.plugin.sdk.model

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

object MainUiCardTypes {
    const val STATUS: String = "STATUS"
    const val KEY_VALUE_LIST: String = "KEY_VALUE_LIST"
    const val ACTION: String = "ACTION"
}

/**
 * Declarative main-UI card. Original v1 hosts support STATUS and KEY_VALUE_LIST.
 * ACTION requires an updated renderer using [com.weartak.plugin.sdk.MainUiActionContract].
 */
@Parcelize
data class MainUiCardParcel(
    val cardType: String,
    val cardId: String,
    val title: String,
    val subtitle: String? = null,
    val content: Bundle = Bundle(),
) : Parcelable
