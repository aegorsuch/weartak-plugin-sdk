package com.weartak.plugin.sdk

/**
 * Canonical [com.weartak.plugin.sdk.model.MarkerParcel.attributes] keys for marker control-plane calls.
 *
 * [markerId] and [com.weartak.plugin.sdk.model.MarkerParcel.title] are first-class parcel fields;
 * coordinates, type, and remark use the keys below.
 */
object MarkerContract {
    /** CoT / 2525 type string (e.g. `a-f-G`) or enum name (`FRIENDLY`). */
    const val ATTR_MARKER_TYPE: String = "markerType"
    const val ATTR_REMARK: String = "remark"
    const val ATTR_LAT: String = "lat"
    const val ATTR_LON: String = "lon"
}
