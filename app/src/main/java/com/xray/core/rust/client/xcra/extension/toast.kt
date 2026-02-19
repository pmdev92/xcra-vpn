package com.xray.core.rust.client.xcra.extension

import android.content.Context
import android.widget.Toast
import es.dmoral.toasty.Toasty


/**
 * Shows a toast message with the given resource ID.
 *
 * @param message The resource ID of the message to show.
 */
fun Context.toast(message: Int) {
    Toasty.normal(this, message).show()
}

/**
 * Shows a toast message with the given text.
 *
 * @param message The text of the message to show.
 */
fun Context.toast(message: CharSequence) {
    Toasty.normal(this, message).show()
}

/**
 * Shows a toast message with the given resource ID.
 *
 * @param message The resource ID of the message to show.
 */
fun Context.toastSuccess(message: Int) {
    Toasty.success(this, message, Toast.LENGTH_SHORT, true).show()
}

/**
 * Shows a toast message with the given text.
 *
 * @param message The text of the message to show.
 */
fun Context.toastSuccess(message: CharSequence) {
    Toasty.success(this, message, Toast.LENGTH_SHORT, true).show()
}

/**
 * Shows a toast message with the given resource ID.
 *
 * @param message The resource ID of the message to show.
 */
fun Context.toastError(message: Int) {
    Toasty.error(this, message, Toast.LENGTH_SHORT, true).show()
}

/**
 * Shows a toast message with the given text.
 *
 * @param message The text of the message to show.
 */
fun Context.toastError(message: CharSequence) {
    Toasty.error(this, message, Toast.LENGTH_SHORT, true).show()
}

