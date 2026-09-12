package com.xray.core.rust.client.xcra.extension

import android.app.Application
import androidx.annotation.StringRes
import com.xray.core.rust.client.xcra.R

fun Application.getErrorMessage(@StringRes id: Int): String {
    return this.getString(
        R.string.error_invalid_field,
        this.getString(id).lowercase(),
    )
}