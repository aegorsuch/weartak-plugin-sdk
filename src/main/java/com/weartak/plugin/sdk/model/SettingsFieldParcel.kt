package com.weartak.plugin.sdk.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Single settings field for hosted settings (design §6.6).
 *
 * Plugin-field visibility: when both [visibleWhenKey] and [visibleWhenLiteral] are set, the field
 * is shown when `key == literal` or `key != literal` per [visibilityNegated] (toggle values only).
 *
 * Host-field visibility: [visibleWhenHostKey] / [visibleWhenHostLiteral] use [HostSettingsContract]
 * keys on [HostSettingsParcel] (string compare for text fields, boolean for connectivity keys).
 *
 * Host binding: when [hostSettingKey] is set, [HostSettingsResolver] can populate the field from
 * [HostSettingsParcel]. [readOnly] rejects plugin patches to that field unless the host recomputes.
 */
@Parcelize
data class SettingsFieldParcel(
    val fieldId: String,
    val label: String,
    /** One of [SettingsFieldTypes]. */
    val fieldType: String,
    val options: List<SettingsOptionParcel> = emptyList(),
    val visibleWhenKey: String? = null,
    val visibleWhenLiteral: String? = null,
    /** If true, visibility uses `!=` instead of `==` between key and literal. */
    val visibilityNegated: Boolean = false,
    val visibleWhenHostKey: String? = null,
    val visibleWhenHostLiteral: String? = null,
    val hostVisibilityNegated: Boolean = false,
    /** [com.weartak.plugin.sdk.HostSettingsContract] key; value derived from host snapshot. */
    val hostSettingKey: String? = null,
    val readOnly: Boolean = false,
    val required: Boolean = false,
    /** Type-appropriate default literal when no previous value exists (e.g. `true`, `fast`, `1.0`). */
    val defaultValueLiteral: String? = null,
    /** When true, host should restart the plugin after apply (design Phase 3). */
    val requiresRestart: Boolean = false,
    val helpText: String? = null,
) : Parcelable
