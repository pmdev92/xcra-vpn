package com.xray.core.rust.client.xcra.dto.core.router

import com.google.gson.annotations.SerializedName

data class Router(
    @SerializedName("rules")
    var rules: MutableList<Rule>? = null
) {
    data class Rule(
        @SerializedName("outbound_tag")
        var outbound: String,
        var domain: List<String>? = null,
        var ip: List<String>? = null,
        var port: String? = null,
        var protocol: List<String>? = null,
        var network: List<String>? = null,
    )
}