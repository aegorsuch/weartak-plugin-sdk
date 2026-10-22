# Keep AIDL stubs and contract parcelables for release consumers (host/plugins).
-keep class com.weartak.plugin.sdk.aidl.** { *; }
-keep class com.weartak.plugin.sdk.lifecycle.** { *; }
-keep class com.weartak.plugin.sdk.binder.** { *; }
-keep class com.weartak.plugin.sdk.model.** implements android.os.Parcelable {
    public static final ** CREATOR;
}
