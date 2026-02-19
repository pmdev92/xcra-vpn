package com.xray.core.rust.client.xcra.util

import android.util.Base64
import com.xray.core.rust.client.xcra.App

object Base64Util {
    /**
     * Decode a base64 encoded string.
     *
     * @param text The base64 encoded string.
     * @return The decoded string, or an empty string if decoding fails.
     */
    fun decode(text: String?): String {
        return tryDecodeBase64(text) ?: text?.trimEnd('=')?.let { tryDecodeBase64(it) }.orEmpty()
    }

    /**
     * Try to decode a base64 encoded string.
     *
     * @param text The base64 encoded string.
     * @return The decoded string, or null if decoding fails.
     */
    fun tryDecodeBase64(text: String?): String? {
        if (text.isNullOrEmpty()) return null

        try {
            return Base64.decode(text, Base64.NO_WRAP).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            App.log("Failed to decode standard base64 $e")
        }
        try {
            return Base64.decode(text, Base64.NO_WRAP.or(Base64.URL_SAFE)).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            App.log("Failed to decode URL-safe base64 $e")
        }
        return null
    }

    /**
     * Encode a string to base64.
     *
     * @param text The string to encode.
     * @param removePadding
     * @return The base64 encoded string, or an empty string if encoding fails.
     */
    fun encode(text: String, removePadding: Boolean = false): String {
        return try {
            var encoded = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            if (removePadding) {
                encoded = encoded.trimEnd('=')
            }
            encoded
        } catch (e: Exception) {
            App.log("Failed to encode text to base64 $e")
            ""
        }
    }
}