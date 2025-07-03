package com.ritesh.cashiro.presentation.ui.extras.components.onBoarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.presentation.ui.theme.*
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.R


@Composable
fun OnBoardingColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Macchiato_Blue, Macchiato_Red, Macchiato_Green, Macchiato_Yellow,
        Macchiato_Mauve, Macchiato_Pink, Macchiato_Teal, Macchiato_Peach,
        Latte_Blue, Latte_Red, Latte_Green, Latte_Yellow, Latte_Pink, Latte_Teal
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(colors) { index, color ->
            val isSelected = selectedColor == color

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        painter = painterResource(R.drawable.verify_bulk),
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnBoardingAccountColorPicker(
    selectedColor1: Color?,
    selectedColor2: Color?,
    onColor1Selected: (Color) -> Unit,
    onColor2Selected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPairs = listOf(
        Color(0xFF696eff) to Color(0xFF93D7DE),
        Color(0xFF6420AA) to Color(0xFFFF3EA5),
        Color(0xFF83C6A4) to Color(0xFFf8acff),
        Color(0xFFff0f7b) to Color(0xFFf89b29),
        Color(0xFFff930f) to Color(0xFFfff95b),
        Color(0xFFf9b16e) to Color(0xFFf68080),
        Macchiato_Blue to Macchiato_Mauve,
        Macchiato_Red to Macchiato_Maroon,
        Macchiato_Green to Macchiato_Teal,
        Macchiato_Yellow to Macchiato_Peach
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(colorPairs) { index, (color1, color2) ->
            val isSelected = selectedColor1 == color1 && selectedColor2 == color2

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onColor1Selected(color1)
                        onColor2Selected(color2)
                    }
            ) {
                // Gradient preview
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(color1, color2),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.verify_bulk),
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountPreviewCard(
    accountName: String,
    balance: String,
    currency: String,
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_animation")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(color1, color2),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Animated overlay for visual appeal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f * shimmer),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f * (1f - shimmer))
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Account name
                Text(
                    text = accountName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = iosFont,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Balance section
                Column {
                    Text(
                        text = "Balance",
                        fontSize = 12.sp,
                        fontFamily = iosFont,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CurrencySymbols.getSymbol(currency),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = iosFont,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = balance,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = iosFont,
                            color = Color.White
                        )
                    }
                }

                // Main account indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.star_bulk),
                        contentDescription = "Main Account",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Main Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = iosFont,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OnBoardingStepIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val isCompleted = index < currentStep

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
            )

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                )
            }
        }
    }
}