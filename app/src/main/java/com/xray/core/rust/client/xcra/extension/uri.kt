package com.xray.core.rust.client.xcra.extension

import java.net.URI

val URI.idnHost: String
    get() = host?.replace("[", "")?.replace("]", "").orEmpty()