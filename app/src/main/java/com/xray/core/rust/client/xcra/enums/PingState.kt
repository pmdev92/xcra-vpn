package com.xray.core.rust.client.xcra.enums


enum class PingState(val value: Int) {
    NOT_MEASURED(0xFF1),
    MEASURING(0xFF2),
    MEASURED_SUCCESS(0xFF3),
    MEASURED_TIMEOUT(0xFF4);

    companion object {
        fun fromInt(value: Int) =
            PingState.entries.firstOrNull { it.value == value }
    }
}

