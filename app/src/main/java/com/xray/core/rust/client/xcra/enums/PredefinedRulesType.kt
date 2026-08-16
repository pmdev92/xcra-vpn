package com.xray.core.rust.client.xcra.enums


enum class PredefinedRulesType(val displayName: String, val fileName: String) {
    WHITE_IRAN("Iran Whitelist", "custom_routing_white_iran"),
    WHITE_CHINA("China Whitelist", "custom_routing_white_china"),
    BLACK_CHINA("China Blacklist", "custom_routing_black_china"),
    GLOBAL("Global", "custom_routing_black");

}
