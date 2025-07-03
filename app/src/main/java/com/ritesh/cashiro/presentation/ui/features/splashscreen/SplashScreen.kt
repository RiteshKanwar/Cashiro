package com.ritesh.cashiro.presentation.ui.features.splashscreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat.Surface
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingViewModel
import com.ritesh.cashiro.presentation.ui.theme.Amoled_Base
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onBoardingViewModel: OnBoardingViewModel,
    onNavigateToOnBoarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    SplashContent(
        isOnBoardingCompleted = onBoardingViewModel::isOnBoardingCompleted,
        onNavigateToOnBoarding = onNavigateToOnBoarding,
        onNavigateToHome = onNavigateToHome,
        modifier = modifier
    )
}

@Composable
private fun SplashContent(
    isOnBoardingCompleted: suspend () -> Boolean,
    onNavigateToOnBoarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
){
    val themeColors = MaterialTheme.colorScheme

    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Alpha animation for fade in effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    var animationProgress by remember { mutableStateOf(false) }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (animationProgress) themeColors.background else Color.Black,
        animationSpec = tween(
            durationMillis = 3000,
            easing = FastOutSlowInEasing
        ),
        label = "background_color_animation"
    )
    val animatedOnBackgroundColor by animateColorAsState(
        targetValue = if (animationProgress) themeColors.surfaceBright else Amoled_Base,
        animationSpec = tween(
            durationMillis = 3000,
            easing = FastOutSlowInEasing
        ),
        label = "onBackground_color_animation"
    )

    LaunchedEffect(Unit) {
        animationProgress = true
    }

    // Check onboarding status and navigate after delay
    LaunchedEffect(Unit) {
        delay(3000)

        try {
            val isOnBoardingCompleted = isOnBoardingCompleted()

            if (isOnBoardingCompleted) {
                onNavigateToHome()
            } else {
                onNavigateToOnBoarding()
            }
        } catch (e: Exception) {
            onNavigateToOnBoarding()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo with breathing animation
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = scale + 0.5f
                        scaleY = scale + 0.5f
                    },
                contentAlignment = Alignment.Center
            ) {
                Spacer(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            color = animatedOnBackgroundColor.copy(0.5f),
                            shape = CircleShape
                        ),
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = animatedOnBackgroundColor,
                            shape = CircleShape
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Welcome",
                        modifier = Modifier.size(120.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}
@Preview(
    name = "Splash Screen - Light Theme",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun SplashScreenLightPreview() {
    MaterialTheme {

            SplashContent(
                isOnBoardingCompleted = { true },
                onNavigateToOnBoarding = { },
                onNavigateToHome = { },
                modifier = Modifier)

    }
}

@Preview(
    name = "Splash Screen - Dark Theme",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SplashScreenDarkPreview() {
    MaterialTheme {
        SplashContent(
                isOnBoardingCompleted = { false },
                onNavigateToOnBoarding = { },
                onNavigateToHome = { },
                modifier = Modifier
        )

    }
}
