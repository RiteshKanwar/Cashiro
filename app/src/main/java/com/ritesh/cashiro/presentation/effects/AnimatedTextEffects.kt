package com.ritesh.cashiro.presentation.effects

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val ANIMATION_DURATION = 1000

@Composable
fun AnimatedCounterText(
    amount: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    maxLines: Int = 1,
    fontFamily: FontFamily? = FontFamily.Default,
    fontWeight: FontWeight? = FontWeight.Normal,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    ),
    textStyle: TextStyle = TextStyle.Default.copy(
        textMotion = TextMotion.Animated,
    ),
    specialKeys: Set<Char> = setOf('+', '-', '*', '/', '(', ')', '%', '×', '÷'),
    onAnimationComplete: () -> Unit = {},
    enableDynamicSizing: Boolean = true // New parameter to enable/disable dynamic sizing
) {
    // Parse the input amount and clean it up
    val cleanedAmount = if (amount.isEmpty() || amount.any { it in specialKeys } || amount.all { it == '0' }) {
        "0"
    } else {
        val trimmedInput = amount.trimStart('0').ifEmpty { "0" }
        if (trimmedInput.contains('.')) {
            val (integerPart, fractionalPart) = trimmedInput.split('.')
            val cleanedFractionalPart = fractionalPart.trimEnd('0')
            if (cleanedFractionalPart.isEmpty()) {
                integerPart
            } else {
                "$integerPart.$cleanedFractionalPart"
            }
        } else {
            trimmedInput
        }
    }

    // Convert to numeric value for animation
    val targetValue = try {
        cleanedAmount.toFloat()
    } catch (e: NumberFormatException) {
        0f
    }

    // Track first appearance to ensure animation runs on initial display
    var isFirstAppearance by remember { mutableStateOf(true) }

    // Remember the previous amount to force animation when mode changes
    var previousAmount by remember { mutableStateOf("") }

    // Force animation start from zero on first appearance or mode change
    val startValue = if (isFirstAppearance || previousAmount != amount) 0f else targetValue

    // Animation target state with key to force recomposition
    val animatedValue by animateFloatAsState(
        targetValue = startValue,
        animationSpec = animationSpec,
        label = "Counter Animation",
        finishedListener = { onAnimationComplete() }
    )

    // Update tracking states after composition
    LaunchedEffect(amount) {
        if (previousAmount != amount) {
            previousAmount = amount
            if (isFirstAppearance) {
                isFirstAppearance = false
            }
        }
    }

    // Format the animated value properly
    val displayText = formatAnimatedValue(animatedValue, cleanedAmount)

    // Calculate dynamic font size based on display text length
    val dynamicFontSize = remember(displayText, enableDynamicSizing) {
        if (enableDynamicSizing) {
            calculateDynamicFontSizeForAnimatedCounterText(displayText)
        } else {
            fontSize.value.toInt()
        }
    }

    Text(
        text = " $displayText", // Add space prefix to match your original usage
        modifier = modifier,
        style = textStyle,
        fontSize = dynamicFontSize.sp, // Use dynamic font size
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        color = if (amount.isEmpty())
            MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
        else
            MaterialTheme.colorScheme.inverseSurface
    )
}

// Helper function to format the animated value to match the target format
private fun formatAnimatedValue(value: Float, targetString: String): String {
    val hasDecimal = targetString.contains('.')
    return if (hasDecimal) {
        val decimalPart = targetString.substringAfter('.', "")
        val decimalPlaces = decimalPart.length
        val factor = 10.0.pow(decimalPlaces.toDouble()).toFloat()
        val roundedValue = (value * factor).roundToLong() / factor

        // Special handling for trailing zeros, similar to the reference code
        if (decimalPart.all { it == '0' }) {
            // If all decimal places are zeros, format as integer with commas
            String.format(Locale.US, "%,d", roundedValue.roundToLong())
        } else if (roundedValue.roundToLong().toFloat() != roundedValue) {
            // If there's a meaningful decimal part
            val pattern = "%,.${decimalPlaces}f"
            String.format(Locale.US, pattern, roundedValue)
        } else {
            // Otherwise, round to whole number with commas
            String.format(Locale.US, "%,d", roundedValue.roundToLong())
        }
    } else {
        // Format integer with commas
        String.format(Locale.US, "%,d", value.roundToLong())
    }
}

// Calculate dynamic font size based on amount length - updated for AnimatedCounterText
fun calculateDynamicFontSizeForAnimatedCounterText(amount: String): Int {
    // Remove commas and spaces for length calculation
    val cleanAmount = amount.replace(",", "").replace(" ", "")
    val length = cleanAmount.length

    return when {
        length <= 4 -> 50      // Very small amounts: largest font
        length <= 6 -> 45      // Small amounts: large font
        length <= 8 -> 38      // Medium amounts: medium-large font
        length <= 10 -> 32     // Longer amounts: medium font
        length <= 12 -> 26     // Long amounts: smaller font
        length <= 15 -> 22     // Very long amounts: small font
        length <= 18 -> 18     // Extremely long amounts: very small font
        else -> 14             // Ultra long amounts: minimum readable size
    }
}

@Composable
fun BlurAnimatedTextCountUpWithCurrency(
    text: String,
    currencySymbol: String = "",
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    maxLines: Int = 1,
    fontFamily: FontFamily? = FontFamily.Default,
    fontWeight: FontWeight? = FontWeight.Normal,
    color: Color = if (text.isEmpty()) MaterialTheme.colorScheme.inverseSurface.copy(0.5f) else MaterialTheme.colorScheme.inverseSurface,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    ),
    textStyle: TextStyle = TextStyle.Default.copy(
        textMotion = TextMotion.Animated
    ),
    onAnimationComplete: () -> Unit = {},
    enableDynamicSizing: Boolean = true // Add dynamic sizing option
) {
    // Track first appearance to ensure animation runs on initial display
    var isFirstAppearance by remember { mutableStateOf(true) }

    // Remember the previous text to force animation when text changes
    var previousText by remember { mutableStateOf("") }

    // Determine the target value
    val targetValue = remember(text) {
        text.replace(currencySymbol, "").toFloatOrNull() ?: text.length.toFloat()
    }

    // Animate the main value
    val animatedValue by animateFloatAsState(
        targetValue = if (isFirstAppearance || previousText != text) 0f else targetValue,
        animationSpec = animationSpec,
        label = "Counter Animation",
        finishedListener = {
            if (isFirstAppearance || previousText != text) {
                onAnimationComplete()
            }
        }
    )
    val isAnimationComplete = animatedValue == targetValue

    // Update tracking states after composition
    LaunchedEffect(text) {
        if (previousText != text) {
            previousText = text
            if (isFirstAppearance) {
                isFirstAppearance = false
            }
        }
    }

    // Format the animated value
    val displayText = remember(animatedValue, text) {
        formatAnimatedText(animatedValue.toDouble(), text, currencySymbol)
    }

    // Calculate dynamic font size for blur text component
    val dynamicFontSize = remember(displayText, enableDynamicSizing) {
        if (enableDynamicSizing) {
            calculateDynamicFontSizeForBlurAnimatedCounterText(displayText)
        } else {
            fontSize.value.toInt()
        }
    }

    // Control blur effect with infinite transition
    val blurList = displayText.mapIndexed { index, character ->
        val infiniteTransition = rememberInfiniteTransition(label = "blur transition $index")
        // Calculate base blur based on animation progress
        val progress = animatedValue / targetValue
        val characterProgress = index.toFloat() / (displayText.length)

        val baseBlur = if (characterProgress <= progress && !isAnimationComplete && character != ' ') {
            infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(ANIMATION_DURATION, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset((ANIMATION_DURATION / displayText.length) * index)
                ),
                label = "Blur Animation"
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }

        baseBlur
    }

    Row(
        modifier = modifier.basicMarquee(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        displayText.forEachIndexed { index, character ->
            Text(
                text = character.toString(),
                modifier = Modifier
                    .padding(end = if (index == displayText.length - 1) 5.dp else 0.dp) // needed for better blur for the end character
                    .graphicsLayer {
                        if (character != ' ') {
                            val blurAmount = blurList[index].value
                            renderEffect = BlurEffect(
                                radiusX = blurAmount,
                                radiusY = blurAmount,
                                edgeTreatment = TileMode.Decal
                            )
                        }
                    },
                style = textStyle,
                fontSize = dynamicFontSize.sp, // Use dynamic font size
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                color = color
            )
        }
    }
}

private fun formatAnimatedText(value: Double, targetText: String, currency: String = ""): String {
    return try {
        val numericValue = targetText.replace(currency, "").trim().toDoubleOrNull()
        if (numericValue != null) {
            // Determine if the original text was negative
            val isNegative = targetText.startsWith("-")

            // Numeric text handling
            val hasDecimal = targetText.contains('.')
            val formattedValue = if (hasDecimal) {
                val decimalPart = targetText.substringAfter('.', "")
                val decimalPlaces = decimalPart.length

                // Special handling for trailing zeros
                val roundedValue = when {
                    // If all decimal places are zeros (like 505.00 or 505.000)
                    decimalPart.all { it == '0' } -> String.format(Locale.US,"%,d", value.roundToLong())
                    // If there's a non-zero decimal part less than 1 cent (like 505.01)
                    value.roundToLong().toDouble() != value -> {
                        val pattern = "%,.${decimalPlaces}f"
                        String.format(pattern, value)
                    }
                    // Otherwise, round to whole number
                    else -> String.format(Locale.US,"%,d", value.roundToLong())
                }

                roundedValue
            } else {
                String.format(Locale.US,"%,d", value.roundToLong())
            }

            // Reconstruct the formatted text with currency and sign
            val revisedFormattedValue = formattedValue.replace("-", "")
            val prefix = if (isNegative) "-$currency" else currency
            "$prefix $revisedFormattedValue"
        } else {
            // String text handling (unchanged)
            val progress = value / targetText.length
            val visibleLength = (progress * targetText.length).roundToInt()
            targetText.take(visibleLength)
        }
    } catch (e: Exception) {
        targetText
    }
}
fun calculateDynamicFontSizeForBlurAnimatedCounterText(amount: String): Int {
    // Remove commas and spaces for length calculation
    val cleanAmount = amount.replace(",", "").replace(" ", "")
    val length = cleanAmount.length

    return when {
        length <= 4 -> 24      // Very small amounts: largest font
        length <= 6 -> 24      // Small amounts: large font
        length <= 8 -> 24     // Medium amounts: medium-large font
        length <= 10 -> 24    // Longer amounts: medium font
        length <= 12 -> 24     // Long amounts: smaller font
        length <= 15 -> 18    // Very long amounts: small font
        length <= 18 -> 16     // Extremely long amounts: very small font
        else -> 14             // Ultra long amounts: minimum readable size
    }
}
//private const val ANIMATION_DURATION = 1000
//@Composable
//fun AnimatedCounterText(
//    amount: String,
//    modifier: Modifier = Modifier,
//    fontSize: TextUnit = 24.sp,
//    maxLines: Int = 1,
//    fontFamily: FontFamily? = FontFamily.Default,
//    fontWeight: FontWeight? = FontWeight.Normal,
//    animationSpec: AnimationSpec<Float> = spring(
//        dampingRatio = Spring.DampingRatioLowBouncy,
//        stiffness = Spring.StiffnessLow
//    ),
//    textStyle: TextStyle = TextStyle.Default.copy(
//        textMotion = TextMotion.Animated,),
//    specialKeys: Set<Char> = setOf('+', '-', '*', '/', '(', ')', '%', '×', '÷'),
//    onAnimationComplete: () -> Unit = {}
//) {
//    // Parse the input amount and clean it up
//    val cleanedAmount = if (amount.isEmpty() || amount.any { it in specialKeys } || amount.all { it == '0' }) {
//        "0"
//    } else {
//        val trimmedInput = amount.trimStart('0').ifEmpty { "0" }
//        if (trimmedInput.contains('.')) {
//            val (integerPart, fractionalPart) = trimmedInput.split('.')
//            val cleanedFractionalPart = fractionalPart.trimEnd('0')
//            if (cleanedFractionalPart.isEmpty()) {
//                integerPart
//            } else {
//                "$integerPart.$cleanedFractionalPart"
//            }
//        } else {
//            trimmedInput
//        }
//    }
//
//    // Convert to numeric value for animation
//    val targetValue = try {
//        cleanedAmount.toFloat()
//    } catch (e: NumberFormatException) {
//        0f
//    }
//
//    // Track first appearance to ensure animation runs on initial display
//    var isFirstAppearance by remember { mutableStateOf(true) }
//
//    // Remember the previous amount to force animation when mode changes
//    var previousAmount by remember { mutableStateOf("") }
//
//    // Force animation start from zero on first appearance or mode change
//    val startValue = if (isFirstAppearance || previousAmount != amount) 0f else targetValue
//
//    // Animation target state with key to force recomposition
//    val animatedValue by animateFloatAsState(
//        targetValue = startValue,
//        animationSpec = animationSpec,
//        label = "Counter Animation",
//        finishedListener = { onAnimationComplete() }
//    )
//
//    // Update tracking states after composition
//    LaunchedEffect(amount) {
//        if (previousAmount != amount) {
//            previousAmount = amount
//            if (isFirstAppearance) {
//                isFirstAppearance = false
//            }
//        }
//    }
//
//    // Format the animated value properly
//    val displayText = formatAnimatedValue(animatedValue, cleanedAmount)
//
//    Text(
//        text = displayText,
//        modifier = modifier,
//        style = textStyle,
//        fontSize = fontSize,
//        maxLines = maxLines,
//        overflow = TextOverflow.Ellipsis,
//        fontWeight = fontWeight,
//        fontFamily = fontFamily,
//        color = if (amount.isEmpty())
//            MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
//        else
//            MaterialTheme.colorScheme.inverseSurface
//    )
//}
//
//// Helper function to format the animated value to match the target format
//private fun formatAnimatedValue(value: Float, targetString: String): String {
//    val hasDecimal = targetString.contains('.')
//    return if (hasDecimal) {
//        val decimalPart = targetString.substringAfter('.', "")
//        val decimalPlaces = decimalPart.length
//        val factor = 10.0.pow(decimalPlaces.toDouble()).toFloat()
//        val roundedValue = (value * factor).roundToLong() / factor
//
//        // Special handling for trailing zeros, similar to the reference code
//        if (decimalPart.all { it == '0' }) {
//            // If all decimal places are zeros, format as integer with commas
//            String.format(Locale.US, "%,d", roundedValue.roundToLong())
//        } else if (roundedValue.roundToLong().toFloat() != roundedValue) {
//            // If there's a meaningful decimal part
//            val pattern = "%,.${decimalPlaces}f"
//            String.format(Locale.US, pattern, roundedValue)
//        } else {
//            // Otherwise, round to whole number with commas
//            String.format(Locale.US, "%,d", roundedValue.roundToLong())
//        }
//    } else {
//        // Format integer with commas
//        String.format(Locale.US, "%,d", value.roundToLong())
//    }
//}
////private fun formatAnimatedValue(value: Float, targetString: String): String {
////    val hasDecimal = targetString.contains('.')
////    return if (hasDecimal) {
////        val decimalPlaces = targetString.substringAfter('.', "").length
////        val factor = 10.0.pow(decimalPlaces.toDouble()).toFloat()
////        val roundedValue = (value * factor).roundToInt() / factor
////        String.format("%.${decimalPlaces}f", roundedValue)
////    } else {
////        value.roundToInt().toString()
////    }
////}
//
//@Composable
//fun BlurAnimatedTextCountUpWithCurrency(
//    text: String,
//    currencySymbol: String = "",
//    modifier: Modifier = Modifier,
//    fontSize: TextUnit = 24.sp,
//    maxLines: Int = 1,
//    fontFamily: FontFamily? = FontFamily.Default,
//    fontWeight: FontWeight? = FontWeight.Normal,
//    color: Color = if (text.isEmpty()) MaterialTheme.colorScheme.inverseSurface.copy(0.5f) else MaterialTheme.colorScheme.inverseSurface,
//    animationSpec: AnimationSpec<Float> = spring(
//        dampingRatio = Spring.DampingRatioLowBouncy,
//        stiffness = Spring.StiffnessLow
//    ),
//    textStyle: TextStyle = TextStyle.Default.copy(
//        textMotion = TextMotion.Animated
//    ),
//    onAnimationComplete: () -> Unit = {}
//) {
//    // Track first appearance to ensure animation runs on initial display
//    var isFirstAppearance by remember { mutableStateOf(true) }
//
//    // Remember the previous text to force animation when text changes
//    var previousText by remember { mutableStateOf("") }
//
//    // Determine the target value
//    val targetValue = remember(text) {
//        text.replace(currencySymbol, "").toFloatOrNull() ?: text.length.toFloat()
//    }
//
//    // Animate the main value
//    val animatedValue by animateFloatAsState(
//        targetValue = if (isFirstAppearance || previousText != text) 0f else targetValue,
//        animationSpec = animationSpec,
//        label = "Counter Animation",
//        finishedListener = {
//            if (isFirstAppearance || previousText != text) {
//                onAnimationComplete()
//            }
//        }
//    )
//    val isAnimationComplete = animatedValue == targetValue
//
//    // Update tracking states after composition
//    LaunchedEffect(text) {
//        if (previousText != text) {
//            previousText = text
//            if (isFirstAppearance) {
//                isFirstAppearance = false
//            }
//        }
//    }
//
//    // Format the animated value
//    val displayText = remember(animatedValue, text) {
//        formatAnimatedText(animatedValue.toDouble(), text, currencySymbol)
//    }
//
//    // Control blur effect with infinite transition
//    val blurList = displayText.mapIndexed { index, character ->
//        val infiniteTransition = rememberInfiniteTransition(label = "blur transition $index")
//        // Calculate base blur based on animation progress
//        val progress = animatedValue / targetValue
//        val characterProgress = index.toFloat() / (displayText.length)
//
//        val baseBlur = if (characterProgress <= progress && !isAnimationComplete && character != ' ') {
//            infiniteTransition.animateFloat(
//                initialValue = 10f,
//                targetValue = 0f,
//                animationSpec = infiniteRepeatable(
//                    animation = tween(ANIMATION_DURATION, easing = LinearOutSlowInEasing),
//                    repeatMode = RepeatMode.Reverse,
//                    initialStartOffset = StartOffset((ANIMATION_DURATION / displayText.length) * index)
//                ),
//                label = "Blur Animation"
//            )
//        } else {
//            remember { mutableFloatStateOf(0f) }
//        }
//
//        baseBlur
//    }
//
//    Row(
//        modifier = modifier.basicMarquee(),
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        displayText.forEachIndexed { index, character ->
//            Text(
//                text = character.toString(),
//                modifier = Modifier
//                    .padding(end = if (index == displayText.length - 1) 5.dp else 0.dp) // needed for better blur for the end character
//                    .graphicsLayer {
//                        if (character != ' ') {
//                            val blurAmount = blurList[index].value
//                            renderEffect = BlurEffect(
//                                radiusX = blurAmount,
//                                radiusY = blurAmount,
//                                edgeTreatment = TileMode.Decal
//                            )
//                        }
//                    },
//                style = textStyle,
//                fontSize = fontSize,
//                maxLines = maxLines,
//                overflow = TextOverflow.Ellipsis,
//                fontWeight = fontWeight,
//                fontFamily = fontFamily,
//                color = color
//            )
//        }
//    }
//}
//private fun formatAnimatedText(value: Double, targetText: String, currency: String = ""): String {
//    return try {
//        val numericValue = targetText.replace(currency, "").trim().toDoubleOrNull()
//        if (numericValue != null) {
//            // Determine if the original text was negative
//            val isNegative = targetText.startsWith("-")
//
//            // Numeric text handling
//            val hasDecimal = targetText.contains('.')
//            val formattedValue = if (hasDecimal) {
//                val decimalPart = targetText.substringAfter('.', "")
//                val decimalPlaces = decimalPart.length
//
//                // Special handling for trailing zeros
//                val roundedValue = when {
//                    // If all decimal places are zeros (like 505.00 or 505.000)
//                    decimalPart.all { it == '0' } -> String.format(Locale.US,"%,d", value.roundToLong())
//                    // If there's a non-zero decimal part less than 1 cent (like 505.01)
//                    value.roundToLong().toDouble() != value -> {
//                        val pattern = "%,.${decimalPlaces}f"
//                        String.format(pattern, value)
//                    }
//                    // Otherwise, round to whole number
//                    else -> String.format(Locale.US,"%,d", value.roundToLong())
//                }
//
//                roundedValue
//            } else {
//                String.format(Locale.US,"%,d", value.roundToLong())
//            }
//
//            // Reconstruct the formatted text with currency and sign
//            val revisedFormattedValue = formattedValue.replace("-", "")
//            val prefix = if (isNegative) "-$currency" else currency
//            "$prefix $revisedFormattedValue"
//        } else {
//            // String text handling (unchanged)
//            val progress = value / targetText.length
//            val visibleLength = (progress * targetText.length).roundToInt()
//            targetText.take(visibleLength)
//        }
//    } catch (e: Exception) {
//        targetText
//    }
//}

//private fun formatAnimatedText(value: Double, targetText: String, currency: String = ""): String {
//    return try {
//        val numericValue = targetText.replace(currency, "").trim().toDoubleOrNull()
//        if (numericValue != null) {
//            // Determine if the original text was negative
//            val isNegative = targetText.startsWith("-")
//
//            // Numeric text handling
//            val hasDecimal = targetText.contains('.')
//            val formattedValue = if (hasDecimal) {
//                val decimalPart = targetText.substringAfter('.', "")
//                val decimalPlaces = decimalPart.length
//
//                // Special handling for trailing zeros
//                val roundedValue = when {
//                    // If all decimal places are zeros (like 505.00 or 505.000)
//                    decimalPart.all { it == '0' } -> value.roundToInt().toString()
//                    // If there's a non-zero decimal part less than 1 cent (like 505.01)
//                    value.roundToInt().toDouble() != value ->
//                        String.format("%.${decimalPlaces}f", value)
//
//                    // Otherwise, round to whole number
//                    else -> value.roundToInt().toString()
//                }
//
//                roundedValue
//            } else {
//                value.roundToInt().toString()
//            }
//
//            // Reconstruct the formatted text with currency and sign
//            val revisedFormattedValue = formattedValue.replace("-", "")
//            val prefix = if (isNegative) "-$currency" else currency
//            "$prefix $revisedFormattedValue"
//        } else {
//            // String text handling (unchanged)
//            val progress = value / targetText.length
//            val visibleLength = (progress * targetText.length).roundToInt()
//            targetText.take(visibleLength)
//        }
//    } catch (e: Exception) {
//        targetText
//    }
//}

