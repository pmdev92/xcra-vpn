package com.xray.core.rust.client.xcra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class XcraColorScheme(
    val configType: Color = Color(0xFFF97910),
    val pingGood: Color = Color(0xFF4CAF50),
    val pingMedium: Color = Color(0xFFFFC107),
    val pingBad: Color = Color(0xFF2196F3),
    val pingTimeout: Color = Color(0xFFF44336),
)

val LightXcraColors = XcraColorScheme(
    pingGood = Color(0xFF4CAF50)
)

val DarkXcraColors = XcraColorScheme(
    pingGood = Color(0xFF4CAF50)
)


object XcraTheme {
    /**
     * Retrieves the current [XcraColorScheme] at the call site's position in the hierarchy.
     */
    val colorScheme: XcraColorScheme
        @Composable @ReadOnlyComposable get() = LocalXcraColors.current

}

val LocalXcraColors = staticCompositionLocalOf<XcraColorScheme> {
    error("XcraColors not provided")
}


@Composable
fun XcraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val xcraColors = if (darkTheme) DarkXcraColors else LightXcraColors
    CompositionLocalProvider(
        LocalXcraColors.provides(xcraColors)
    ) {
        content()
    }
}