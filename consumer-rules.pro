# Keep AIDL stubs and contract parcelables for release consumers (host/plugins).
-keep class com.weartak.plugin.sdk.aidl.** { *; }
-keep class com.weartak.plugin.sdk.lifecycle.** { *; }
-keep class com.weartak.plugin.sdk.binder.** { *; }
-keep class com.weartak.plugin.sdk.model.** implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep class com.weartak.plugin.sdk.mopp.aidl.** { *; }
-keep class com.weartak.plugin.sdk.mopp.model.** implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep class com.weartak.plugin.sdk.routestatus.aidl.** { *; }
-keep class com.weartak.plugin.sdk.routestatus.model.** implements android.os.Parcelable {
    public static final ** CREATOR;
}
