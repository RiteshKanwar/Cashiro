package com.ritesh.cashiro.presentation.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ritesh.cashiro.domain.repository.ThemeMode
import kotlinx.coroutines.launch

private val DarkColorScheme = darkColorScheme(
    primary = Macchiato_Red,
    secondary = Macchiato_Teal,
    onError = ErrorColor,
    background = Macchiato_Crust,
    onBackground = Macchiato_Surface_0,
    surface = Macchiato_Base,
    surfaceBright = Macchiato_Mantle,
    surfaceVariant = Macchiato_Mantle,
    onSurfaceVariant = Macchiato_Crust,
    inverseSurface = Macchiato_Text,
    inverseOnSurface = Macchiato_Overlay_0 ,
    inversePrimary = Macchiato_SubText_0,
    tertiary = SuccessColor, // is used for income
    outline =  ErrorColor, // is used for expense
    )

private val LightColorScheme = lightColorScheme(
    primary = Latte_Red,
    secondary = Latte_Teal,
    onError = Latte_DarkRed,
    background = Latte_Crust,
    onBackground = Latte_Mantle,
    surface = Latte_Base,
    surfaceBright = Latte_Surface_0,
    surfaceVariant = Latte_Base,
    onSurfaceVariant = Latte_Crust,
    inverseSurface = Latte_Text,
    inverseOnSurface = Latte_Overlay_0,
    inversePrimary = Latte_SubText_0,
    tertiary = Latte_Green, // is used for income
    outline =  Latte_DarkRed, // is used for expense
    )

private val BlackColorScheme = darkColorScheme(
    primary = Macchiato_Red,
    secondary = Macchiato_Teal,
    onError = ErrorColor,
    background = Amoled_Crust,  // Pure black
    onBackground = Amoled_Surface_0,
    surface = Amoled_Base,      // Pure black
    onSurfaceVariant = Amoled_Mantle ,
    surfaceBright = Amoled_Mantle, // Pure black
    surfaceVariant = Amoled_Mantle, // Pure black
    inverseSurface = Amoled_Text,
    inverseOnSurface = Amoled_Overlay_0,
    inversePrimary = Amoled_SubText_0,

    tertiary = SuccessColor, // is used for income
    outline =  ErrorColor, // is used for expense
)


// here i want two new color schemes (dark and light) based on wallpaper of the device
private val WallpaperLightColorsScheme = lightColorScheme(

)
private val WallpaperDarkColorsScheme = darkColorScheme(

)

fun updateColorScheme(originalScheme: ColorScheme, newPrimary: Color): ColorScheme {
    return originalScheme.copy(primary = newPrimary)
}

@Composable
fun CashiroTheme(
    themeMode: ThemeMode = ThemeMode.System,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val darkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark, ThemeMode.Black -> true
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.SystemBlack -> isSystemInDarkTheme()
    }

    val useBlackTheme = when(themeMode) {
        ThemeMode.Black -> true
        ThemeMode.SystemBlack -> isSystemInDarkTheme()
        else -> false
    }

    // Create base color scheme
    val colorScheme = when {
        darkTheme && useBlackTheme -> BlackColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val dynamicColorScheme = updateColorScheme(colorScheme, primaryColor)

    // Animation specs
    val animationSpec = tween<Float>(
        durationMillis = 800,
        easing = FastOutSlowInEasing
    )

    // Remember previous theme state to detect changes
    val prevThemeState = remember { mutableStateOf(darkTheme) }

    // Create a blur radius animation
    val blurRadius = remember { Animatable(0f) }

    // Control system UI colors
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )

        systemUiController.setNavigationBarColor(
            color =  Color.Transparent,
            darkIcons = !darkTheme,
            navigationBarContrastEnforced = false
        )
    }

    // Create animated color transitions for smooth color changes
    val backgroundColor = animateColorAsState(
        targetValue = dynamicColorScheme.background,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    val surfaceColor = animateColorAsState(
        targetValue = dynamicColorScheme.surface,
        animationSpec = tween(800, easing = FastOutSlowInEasing)
    )

    // Create a modified color scheme with animated colors
    val animatedColorScheme = dynamicColorScheme.copy(
        background = backgroundColor.value,
        surface = surfaceColor.value
    )

    // Apply the blur effect with animated radius
    Box(
        modifier = Modifier.fillMaxSize()
            .then(
                if (prevThemeState.value != darkTheme) {
                    coroutineScope.launch {
                        blurRadius.snapTo(5f)
                        // Animate to zero blur
                        blurRadius.animateTo(
                            targetValue = 0f,
                            animationSpec = animationSpec
                        )
                    }

                   Modifier.graphicsLayer {
                       // Only apply blur if radius > 0
                       if (blurRadius.value > 0f) {
                           renderEffect = BlurEffect(
                               radiusX = blurRadius.value,
                               radiusY = blurRadius.value,
                               edgeTreatment = TileMode.Mirror
                           )

                           // Adjust alpha for a smoother transition
                           alpha = 0.95f + (0.05f * (1f - blurRadius.value / 10f))
                       }
                   }
                } else{
                    prevThemeState.value = darkTheme
                    Modifier
                }
            )
    ) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            typography = Typography,
            content = content
        )
    }
}
