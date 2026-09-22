package com.xray.core.rust.client.xcra.extension

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.optStringOrNull(key: String): String? =

    if (has(key) && !isNull(key)) getString(key) else null

fun JSONObject.optIntOrNull(key: String): Int? =

    if (has(key) && !isNull(key)) getInt(key) else null

fun JSONObject.optLongOrNull(key: String): Long? =

    if (has(key) && !isNull(key)) getLong(key) else null

fun JSONObject.optDoubleOrNull(key: String): Double? =

    if (has(key) && !isNull(key)) getDouble(key) else null

fun JSONObject.optBooleanOrNull(key: String): Boolean? =

    if (has(key) && !isNull(key)) getBoolean(key) else null

fun JSONObject.optJSONObjectOrNull(key: String): JSONObject? =

    if (has(key) && !isNull(key)) getJSONObject(key) else null

fun JSONObject.optJSONArrayOrNull(key: String): JSONArray? =

    if (has(key) && !isNull(key)) getJSONArray(key) else null