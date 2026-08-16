package com.xray.core.rust.client.xcra.dto

import android.content.Context
import com.xray.core.rust.client.xcra.R
import com.xray.core.rust.client.xcra.extension.toTrafficString
import com.xray.core.rust.client.xcra.util.Utils
import java.io.File
import java.text.DateFormat
import java.util.Date

data class AssetItem(
    var remarks: String = "",
    var url: String = "",
    val addedTime: Long = System.currentTimeMillis(),
    var editTime: Long = -1,
    var locked: Boolean? = false
)

fun getAssetUrlItemProperties(item: AssetItem, context: Context): String {
    val extDir = File(Utils.userAssetPath(context))
    val file = extDir.listFiles()?.find { it.name == item.remarks }

    if (file != null) {
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
        return "${
            file.length().toTrafficString()
        }  •  ${dateFormat.format(Date(file.lastModified()))}"
    } else {
        return context.getString(R.string.msg_file_not_found)
    }
}