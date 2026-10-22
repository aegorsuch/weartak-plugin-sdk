package com.weartak.plugin.sdk.binder

import android.content.Context
import java.io.InputStream

/**
 * Helper for plugin-owned CEP authoring files such as `plugin_rules.json`.
 *
 * The loader is intentionally thin: it reads the JSON artifact and leaves envelope assembly
 * to the plugin runtime that submits the payload to the host.
 */
object PluginRulesAssetLoader {
    const val DEFAULT_ASSET_NAME = "plugin_rules.json"

    @JvmStatic
    fun loadFromAssets(context: Context, assetName: String = DEFAULT_ASSET_NAME): String =
        context.assets.open(assetName).use { loadFromStream(it) }

    @JvmStatic
    fun loadFromStream(stream: InputStream): String =
        stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
}
