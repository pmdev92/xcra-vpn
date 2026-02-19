package com.xray.core.rust.client.xcra.enums

import com.xray.core.rust.client.xcra.dto.AppConfig

enum class AllowInsecure(val displayName: String) {
    FOLLOW_CONFIGURATION(AppConfig.ALLOW_INSECURE_FOLLOW),
    FORCE_INSECURE(AppConfig.ALLOW_INSECURE_FORCE_INSECURE),
    FORCE_SECURE(AppConfig.ALLOW_INSECURE_FORCE_SECURE);

    companion object {
        fun asList(): List<String> {
            val items = mutableListOf<String>()
            for (item in AllowInsecure.entries) {
                items.add(item.displayName)
            }
            return items
        }

        fun fromString(type: String?) =
            AllowInsecure.entries.find { it.displayName == type } ?: FOLLOW_CONFIGURATION
    }
}