package com.ritesh.cashiro.presentation.ui.extras.components.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Rosewater

// Color Picker Composable used in Category bottomSheet
@Composable
fun CategoryBoxColorPicker(
    isDarkTheme: Boolean,
    initialColor: Color,
    onPrimaryColorSelected: (Color) -> Unit,
) {
    val themeColors = MaterialTheme.colorScheme
    var isCustomColorSelected by remember { mutableStateOf(false) }
    var customColor by remember(initialColor) { mutableStateOf(initialColor) }
    val controller = rememberColorPickerController()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle Button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.custom_color_picker),
                contentDescription = "Color Picker",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { isCustomColorSelected = !isCustomColorSelected }
                    .animateContentSize(), // Smooth size adjustment
                tint = Color.Unspecified // Disable tinting
            )

            Spacer(Modifier.width(1.dp).height(18.dp).background(themeColors.inverseSurface.copy(0.3f)))
            // Accent Color Picker
            DefaultCategoryBoxColorPicker(
                isDarkTheme = isDarkTheme,
                onColorSelected = { selectedColor ->
                    customColor = selectedColor
                    onPrimaryColorSelected(selectedColor)
                }
            )
        }

        // Conditional Content
        AnimatedVisibility(visible = isCustomColorSelected) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HSV Color Picker
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 30.dp, vertical = 10.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        customColor = colorEnvelope.color
                        onPrimaryColorSelected(colorEnvelope.color)
                    },
                    initialColor = initialColor
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .height(35.dp),
                    controller = controller
                )
            }
        }
    }
}

@Composable
fun DefaultCategoryBoxColorPicker(
    isDarkTheme: Boolean,
    onColorSelected: (Color) -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    val colors = if (isDarkTheme) {
        listOf(
            themeColors.onBackground,themeColors.primary,Macchiato_Rosewater,
        )
    } else {
        listOf(
            themeColors.onBackground,themeColors.primary,Latte_Blue,
        )
    }

    Box(){
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            colors.forEach { color ->
                item{ Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color) }
                )}
            }
        }
        Spacer(
            modifier = Modifier
                .padding(start = 6.dp)
                .height(56.dp)
                .width(10.dp)
                .background(
                    brush = Brush.horizontalGradient(colors = listOf(themeColors.surface, Color.Transparent))
                ).align(Alignment.TopStart)
        )
        Spacer(
            modifier = Modifier
                .padding(end = 6.dp)
                .height(56.dp)
                .width(10.dp)
                .background(
                    brush = Brush.horizontalGradient(colors = listOf(Color.Transparent,themeColors.surface))
                ).align(Alignment.TopEnd)
        )

    }
}