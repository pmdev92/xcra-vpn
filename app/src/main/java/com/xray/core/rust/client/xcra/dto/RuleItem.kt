package com.xray.core.rust.client.xcra.dto

import com.google.gson.annotations.SerializedName
import com.xray.core.rust.client.xcra.util.Utils


data class RuleItem(
    var id: String = Utils.getUuid(),
    var remarks: String? = "",
    var ip: List<String>? = null,
    var domain: List<String>? = null,
    var port: String? = null,
    var network: List<String>? = null,
    var protocol: List<String>? = null,
    var enabled: Boolean = true,
    var locked: Boolean? = false,
    @SerializedName("outbound_tag")
    var outboundTag: String = "",
)