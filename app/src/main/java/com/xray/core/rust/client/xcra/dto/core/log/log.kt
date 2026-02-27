package com.xray.core.rust.client.xcra.dto.core.log

import com.google.gson.annotations.SerializedName

data class Log(
    @SerializedName("level")
    var level: String = "warning",
)

