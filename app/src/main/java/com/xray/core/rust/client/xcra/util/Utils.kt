package com.xray.core.rust.client.xcra.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Patterns
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.xray.core.rust.client.xcra.App
import com.xray.core.rust.client.xcra.dto.AppConfig
import com.xray.core.rust.client.xcra.util.IpUtil.isIpInCidr
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object Utils {
    /**
     * Write string data to file.
     *
     */
    fun printToFile(file: File, data: String?) {
        try {
            val printWriter = PrintWriter(file)
            printWriter.println(data)
            printWriter.close()
        } catch (e: FileNotFoundException) {
            e.fillInStackTrace()
        }
    }


    /**
     * Generate a UUID.
     *
     * @return A UUID string without dashes.
     */
    fun getUuid(): String {
        return try {
            UUID.randomUUID().toString().replace("-", "")
        } catch (e: Exception) {
            App.log("Failed to generate UUID : $e")
            ""
        }
    }

    /**
     * Set text to the clipboard.
     *
     * @param context The context to use.
     * @param content The text to set to the clipboard.
     */
    fun setClipboard(context: Context, content: String) {
        try {
            val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(null, content)
            cmb.setPrimaryClip(clipData)
        } catch (e: Exception) {
            App.log("Failed to set clipboard content $e")
        }
    }

    /**
     * Parse a string to an integer with a default value.
     *
     * @param str The string to parse.
     * @param default The default value if parsing fails.
     * @return The parsed integer, or the default value if parsing fails.
     */
    fun parseInt(str: String?, default: Int = 0): Int {
        return str?.toIntOrNull() ?: default
    }

    /**
     * Get text from the clipboard.
     *
     * @param context The context to use.
     * @return The text from the clipboard, or an empty string if an error occurs.
     */
    fun getClipboard(context: Context): String {
        return try {
            val cmb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cmb.primaryClip?.getItemAt(0)?.text.toString()
        } catch (e: Exception) {
            App.log("Failed to get clipboard content $e")
            ""
        }
    }

    /**
     * Check if a string is a valid URL.
     *
     * @param value The string to check.
     * @return True if the string is a valid URL, false otherwise.
     */
    fun isValidUrl(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false
        return try {
            Patterns.WEB_URL.matcher(value).matches() ||
                    Patterns.DOMAIN_NAME.matcher(value).matches() ||
                    URLUtil.isValidUrl(value)
        } catch (e: Exception) {
            App.log("Failed to validate URL $e")
            false
        }
    }

    /**
     * Open a URI in a browser.
     *
     * @param context The context to use.
     * @param uriString The URI string to open.
     */
    fun openUri(context: Context, uriString: String) {
        try {
            val uri = uriString.toUri()
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            App.log("Failed to open URI $e")
        }
    }


    /**
     * Decode a URL-encoded string.
     *
     * @param url The URL-encoded string.
     * @return The decoded string, or the original string if decoding fails.
     */
    fun urlDecode(url: String): String {
        return try {
            URLDecoder.decode(url, Charsets.UTF_8.toString())
        } catch (e: Exception) {
            App.log("Failed to decode URL $e")
            url
        }
    }

    /**
     * Encode a string to URL-encoded format.
     *
     * @param url The string to encode.
     * @return The URL-encoded string, or the original string if encoding fails.
     */
    fun urlEncode(url: String): String {
        return try {
            URLEncoder.encode(url, Charsets.UTF_8.toString()).replace("+", "%20")
        } catch (e: Exception) {
            App.log("Failed to encode URL $e")
            url
        }
    }

    /**
     * Get the path to the backup directory.
     *
     * @param context The context to use.
     * @return The path to the backup directory.
     */
    fun backupPath(context: Context?): String {
        if (context == null) return ""

        return try {
            context.getExternalFilesDir(AppConfig.DIR_BACKUPS)?.absolutePath
                ?: context.getDir(AppConfig.DIR_BACKUPS, 0).absolutePath
        } catch (e: Exception) {
            App.log("Failed to get backup path $e")
            ""
        }
    }

    /**
     * Get the IPv6 address in a formatted string.
     *
     * @param address The IPv6 address.
     * @return The formatted IPv6 address, or the original address if not valid.
     */
    fun getIpv6Address(address: String?): String {
        if (address.isNullOrEmpty()) return ""

        return if (IpUtil.isIpv6Address(address) && !address.contains('[') && !address.contains(']')) {
            "[$address]"
        } else {
            address
        }
    }

    /**
     * Fix illegal characters in a URL.
     *
     * @param str The URL string.
     * @return The URL string with illegal characters replaced.
     */
    fun fixIllegalUrl(str: String): String {
        return str.replace(" ", "%20")
            .replace("|", "%7C")
    }

    /**
     * Check if a string is a valid subscription URL.
     *
     * @param value The string to check.
     * @return True if the string is a valid subscription URL, false otherwise.
     */
    fun isValidSubscriptionUrl(value: String?): Boolean {
        if (value.isNullOrEmpty()) return false

        try {
            if (URLUtil.isHttpsUrl(value)) return true
            if (URLUtil.isHttpUrl(value)) {
                if (value.contains(AppConfig.LOOPBACK_IP)) return true

                //Check private ip address
                val uri = URI(fixIllegalUrl(value))
                if (IpUtil.isIpAddress(uri.host)) {
                    AppConfig.PRIVATE_IP_LIST.forEach {
                        if (isIpInCidr(uri.host, it)) return true
                    }
                }
            }
        } catch (e: Exception) {
            App.log("Failed to validate subscription URL $e")
        }
        return false
    }

    /**
     * Get the receiver flags based on the Android version.
     *
     * @return The receiver flags.
     */
    fun receiverFlags(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.RECEIVER_EXPORTED
    } else {
        ContextCompat.RECEIVER_NOT_EXPORTED
    }

    fun getDateTimeFormated(): String {
        val formatter = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}

