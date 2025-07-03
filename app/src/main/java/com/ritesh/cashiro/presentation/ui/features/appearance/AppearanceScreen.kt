package com.ritesh.cashiro.presentation.ui.features.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import com.ritesh.cashiro.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.ritesh.cashiro.domain.repository.LabelVisibility
import com.ritesh.cashiro.domain.repository.Settings
import com.ritesh.cashiro.domain.repository.ThemeMode
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Latte_Flamingo
import com.ritesh.cashiro.presentation.ui.theme.Latte_Green
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Latte_Mauve
import com.ritesh.cashiro.presentation.ui.theme.Latte_Peach
import com.ritesh.cashiro.presentation.ui.theme.Latte_Pink
import com.ritesh.cashiro.presentation.ui.theme.Latte_Red
import com.ritesh.cashiro.presentation.ui.theme.Latte_Rosewater
import com.ritesh.cashiro.presentation.ui.theme.Latte_Teal
import com.ritesh.cashiro.presentation.ui.theme.Latte_Yellow
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Flamingo
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Lavender
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Pink
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Rosewater
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Sapphire
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Sky
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Yellow
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.theme.Amoled_Base
import com.ritesh.cashiro.presentation.ui.theme.Amoled_Crust
import com.ritesh.cashiro.presentation.ui.theme.Latte_Base
import com.ritesh.cashiro.presentation.ui.theme.Latte_Crust
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Base
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Crust


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppearanceScreen(
    onBackClicked: () -> Unit,
    appearanceViewModel: AppearanceViewModel,
    screenTitle: String,
    previousScreenTitle: String
) {
    val settings by appearanceViewModel.settings.collectAsState()
    AppearanceContent(
        settings = settings,
        updatePrimaryColor = appearanceViewModel::updatePrimaryColor,
        updateLabelVisibility = appearanceViewModel::updateLabelVisibility,
        updateThemeMode = appearanceViewModel::updateThemeMode,
        updateShowProfileBanner = appearanceViewModel::updateShowProfileBanner,
        onBackClicked = onBackClicked,
        screenTitle = screenTitle,
        previousScreenTitle = previousScreenTitle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceContent(
    settings: Settings,
    updatePrimaryColor: (Color) -> Unit,
    updateLabelVisibility: (LabelVisibility) -> Unit,
    updateThemeMode: (ThemeMode) -> Unit,
    updateShowProfileBanner: (Boolean) -> Unit,
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String
){

    val themeColors = MaterialTheme.colorScheme
    val scrollOverScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
// Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall =  scrollBehaviorSmall,
                title = screenTitle,
                previousScreenTitle = previousScreenTitle,
                onBackClick = onBackClicked,
                hasBackButton = true
            )

        },

        content = { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
            ){
                LazyColumn(
                    state = scrollOverScrollState,
                    userScrollEnabled = false,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .overscroll(overscrollEffect)
                            .scrollable(
                                orientation = Orientation.Vertical,
                                reverseDirection = true,
                                state = scrollOverScrollState,
                                overscrollEffect = overscrollEffect
                            ),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    item(key = "Accent Colors") {
                        AccentColors(
                            settings = settings,
                            themeColors = themeColors,
                            updatePrimaryColor = updatePrimaryColor
                        )
                    }
                    item(key = "Navigation Bar Styles") {
                        NavigationBarStyles(
                            settings = settings,
                            themeColors = themeColors,
                            updateLabelVisibility = updateLabelVisibility
                        )
                    }
                    item(key = "App Themes") {
                        AppThemes(
                            settings = settings,
                            themeColors = themeColors,
                            updateThemeMode = updateThemeMode
                        )
                    }
                    item(key = "ProfileBanner In HomeScreen") {
                        ProfileDisplaySettings(
                            settings = settings,
                            themeColors = themeColors,
                            updateShowProfileBanner = updateShowProfileBanner
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .height(130.0.dp)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    themeColors.background,
                                    themeColors.background,
                                    themeColors.background.copy(0.9f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

            }
        }
    )
}

@Composable
fun AccentColors(
    updatePrimaryColor: (Color) -> Unit,
    settings: Settings,
    themeColors: ColorScheme
){
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Accent Colors",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.surface, shape = RoundedCornerShape(16.dp))
        ) {
            PrimaryColorPicker(
                settings = settings,
                isDarkTheme = isSystemInDarkTheme(),
                initialColor = settings.primaryColor,
                onPrimaryColorSelected = { selectedColor ->
                    updatePrimaryColor(selectedColor)
                }
            )
        }
    }
}

@Composable
fun NavigationBarStyles(
    updateLabelVisibility: (LabelVisibility) -> Unit,
    settings: Settings,
    themeColors: ColorScheme
){
    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "NavigationBar Styles",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = themeColors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.clickable {
                updateLabelVisibility(LabelVisibility.AlwaysShow)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AlwaysShowLabel(isSelected = settings.labelVisibility == LabelVisibility.AlwaysShow)
                    Text(
                        text = "Always Show",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontFamily = iosFont,
                        fontSize = 10.sp,
                        color = if (settings.labelVisibility == LabelVisibility.AlwaysShow) themeColors.primary
                        else themeColors.inverseSurface.copy(0.5f)
                    )
                }
            }

            Box(modifier = Modifier.clickable {
                updateLabelVisibility(LabelVisibility.SelectedOnly)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SelectedOnlyLabel(isSelected = settings.labelVisibility == LabelVisibility.SelectedOnly)
                    Text(
                        text = "Selected Only",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontFamily = iosFont,
                        fontSize = 10.sp,
                        color = if (settings.labelVisibility == LabelVisibility.SelectedOnly) themeColors.primary
                        else themeColors.inverseSurface.copy(0.5f)
                    )
                }
            }

            Box(modifier = Modifier.clickable {
                updateLabelVisibility(LabelVisibility.NeverShow)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NeverShowLabel(isSelected = settings.labelVisibility == LabelVisibility.NeverShow)
                    Text(
                        text = "Hide Labels",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontFamily = iosFont,
                        fontSize = 10.sp,
                        color = if (settings.labelVisibility == LabelVisibility.NeverShow) themeColors.primary
                        else themeColors.inverseSurface.copy(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AppThemes(
    updateThemeMode: (ThemeMode) -> Unit,
    settings: Settings,
    themeColors: ColorScheme
){
    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "App Theme",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = themeColors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            ThemeSelector(
                currentTheme = settings.themeMode,
                onThemeSelected = { selectedTheme ->
                    updateThemeMode(selectedTheme)
                }
            )
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSelector(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val themeColors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            item {
                ThemeOption(
                    title = "Light Mode",
                    isSelected = currentTheme == ThemeMode.Light,
                    onClick = { onThemeSelected(ThemeMode.Light) },
                    colors = themeColors,
                    backgroundColorLight = Latte_Crust,
                    surfaceColorLight = Latte_Base,
                    backgroundColorDark = Latte_Crust,
                    surfaceColorDark = Latte_Base
                )
            }

            item {
                ThemeOption(
                    title = "Dark Mode",
                    isSelected = currentTheme == ThemeMode.Dark,
                    onClick = { onThemeSelected(ThemeMode.Dark) },
                    colors = themeColors,
                    backgroundColorLight = Macchiato_Crust,
                    surfaceColorLight = Macchiato_Base,
                    backgroundColorDark = Macchiato_Crust,
                    surfaceColorDark = Macchiato_Base
                )
            }
            item {
                ThemeOption(
                    title = "Black Amoled",
                    isSelected = currentTheme == ThemeMode.Black,
                    onClick = { onThemeSelected(ThemeMode.Black) },
                    colors = themeColors,
                    backgroundColorLight = Amoled_Crust,
                    surfaceColorLight = Amoled_Base,
                    backgroundColorDark = Amoled_Crust,
                    surfaceColorDark = Amoled_Base
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalDivider(modifier = Modifier, color= MaterialTheme.colorScheme.inverseSurface.copy(0.1f))
            SystemThemeOption(
                title = "System Default",
                isSelected = currentTheme == ThemeMode.System,
                onClick = { onThemeSelected(ThemeMode.System) },
                colors = themeColors
            )
            HorizontalDivider(modifier = Modifier, color= MaterialTheme.colorScheme.inverseSurface.copy(0.1f))
            SystemThemeOption(
                title = "System Black",
                isSelected = currentTheme == ThemeMode.SystemBlack,
                onClick = { onThemeSelected(ThemeMode.SystemBlack) },
                colors = themeColors
            )
        }
    }

}
@Composable
fun SystemThemeOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: ColorScheme
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontFamily = iosFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.inverseSurface
        )
        Switch(
            checked = isSelected,
            onCheckedChange = { onClick() },
            modifier = Modifier.padding(start = 8.dp),
            thumbContent = null,
            enabled = true,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary,
                uncheckedThumbColor = colors.inverseSurface.copy(0.5f),
                uncheckedTrackColor = colors.background,
                uncheckedBorderColor = Color.Transparent
            ),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}

@Composable
fun ThemeOption(
    backgroundColorLight: Color,
    surfaceColorLight: Color,
    backgroundColorDark: Color,
    surfaceColorDark: Color,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: ColorScheme
){
    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
        .clickable(
            onClick = onClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).size(80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier
                .width(40.dp)
                .height(80.dp)
                .background(
                    color = backgroundColorLight,
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                ),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier
                        .padding(start = 10.dp)
                        .height(10.dp)
                        .width(30.dp)
                        .background(
                            color = surfaceColorLight,
                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                        )
                    )
                    Spacer(modifier = Modifier
                        .padding(start = 20.dp)
                        .height(10.dp)
                        .width(20.dp)
                        .background(
                            color = surfaceColorLight,
                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                        )
                    )
                }
            }
            Box(modifier = Modifier
                .width(40.dp)
                .height(80.dp)
                .background(
                    color = backgroundColorDark,
                    shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                ),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier
                        .padding(end = 10.dp)
                        .height(10.dp)
                        .width(30.dp)
                        .background(
                            color = surfaceColorDark,
                            shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                        )
                    )
                    Spacer(modifier = Modifier
                        .padding(end = 20.dp)
                        .height(10.dp)
                        .width(20.dp)
                        .background(
                            color = surfaceColorDark,
                            shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                        )
                    )
                }
            }
        }
        Text(
            text = title,
            fontSize = 10.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isSelected) colors.primary else colors.inverseSurface.copy(0.5f)
        )

    }
}

@Composable
fun PrimaryColorPicker(
    settings: Settings,
    isDarkTheme: Boolean,
    initialColor: Color,
    onPrimaryColorSelected: (Color) -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    var isCustomColorSelected by remember { mutableStateOf(false) }
    var customColor by remember { mutableStateOf(initialColor) }
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

            Spacer(Modifier
                .width(1.dp)
                .height(18.dp)
                .background(themeColors.inverseSurface.copy(0.3f)))
            // Accent Color Picker
            AccentColorPicker(
                themeMode = settings.themeMode,
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
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HSV Color Picker
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(30.dp),
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
                        .padding(20.dp)
                        .height(35.dp),
                    controller = controller
                )

                Button(
                    onClick = {
                        isCustomColorSelected = false
                        onPrimaryColorSelected(customColor)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary,
                        contentColor = themeColors.inverseSurface
                    )
                ) {
                    Text(
                        text = "Set Color",
                        fontFamily = iosFont,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AccentColorPicker(
    themeMode: ThemeMode = ThemeMode.System,
    isDarkTheme: Boolean,
    onColorSelected: (Color) -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    val colors = when (themeMode){
        ThemeMode.Dark -> listOf(
            Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
            Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
            Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
            Macchiato_Blue, Macchiato_Lavender
        )
        ThemeMode.Light -> listOf(
            Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
            Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
            Latte_Teal, Latte_Green,
        )

        ThemeMode.Black -> listOf(
            Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
            Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
            Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
            Macchiato_Blue, Macchiato_Lavender
        )

        ThemeMode.System -> if (isDarkTheme) {
            listOf(
                Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
                Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
                Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
                Macchiato_Blue, Macchiato_Lavender
            )
        } else {
            listOf(
                Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
                Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
                Latte_Teal, Latte_Green,
            )
        }

        ThemeMode.SystemBlack -> if (isDarkTheme) {
            listOf(
                Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
                Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
                Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
                Macchiato_Blue, Macchiato_Lavender
            )
        } else {
            listOf(
                Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
                Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
                Latte_Teal, Latte_Green,
            )
        }
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
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            themeColors.surface,
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopStart)
        )
        Spacer(
            modifier = Modifier
                .padding(end = 6.dp)
                .height(56.dp)
                .width(10.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeColors.surface
                        )
                    )
                )
                .align(Alignment.TopEnd)
        )

    }
}

@Composable
fun AlwaysShowLabel(isSelected: Boolean = false){

    val themeColors = MaterialTheme.colorScheme
    Box(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))){
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = themeColors.surfaceBright))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(60.dp)
                .background(color = themeColors.background)
                .align(Alignment.BottomCenter))
        Box(
            modifier = Modifier
                .padding(top = 15.dp, bottom = 10.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(
                        0.3f
                    )
                )
                .align(Alignment.Center))
        Box(
            modifier = Modifier
                .width(80.dp)
                .align(Alignment.BottomCenter)){
            Text(
                text = "Abc",
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(0.3f),
                modifier = Modifier.width(80.dp))
        }
    }

}

@Composable
fun SelectedOnlyLabel(isSelected: Boolean = false){

    val themeColors = MaterialTheme.colorScheme
    Box(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))){
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = themeColors.surfaceBright))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(60.dp)
                .background(color = themeColors.background)
                .align(Alignment.BottomCenter))
        Box(
            modifier = Modifier
                .padding(bottom = 23.dp, end = 10.dp)
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    color = if (isSelected) themeColors.primary.copy(0.5f) else themeColors.inverseSurface.copy(
                        0.15f
                    )
                )
                .align(Alignment.BottomEnd))
        Box(
            modifier = Modifier
                .padding(bottom = 23.dp, start = 10.dp)
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    color = if (isSelected) themeColors.primary.copy(0.5f) else themeColors.inverseSurface.copy(
                        0.15f
                    )
                )
                .align(Alignment.BottomStart))
        Box(
            modifier = Modifier
                .padding(top = 15.dp, bottom = 10.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(
                        0.3f
                    )
                )
                .align(Alignment.Center))
        Box(
            modifier = Modifier
                .width(80.dp)
                .align(Alignment.BottomCenter)){
            Text(
                text = "Abc",
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(0.3f),
                modifier = Modifier.width(80.dp))
        }
    }

}

@Composable
fun NeverShowLabel(isSelected: Boolean = false){

    val themeColors = MaterialTheme.colorScheme
    Box(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))){
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = themeColors.surfaceBright))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(60.dp)
                .background(color = themeColors.background)
                .align(Alignment.BottomCenter))
        Box(
            modifier = Modifier
                .padding(top = 15.dp, bottom = 10.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(
                        0.3f
                    )
                )
                .align(Alignment.Center))
    }

}

@Composable
fun ProfileDisplaySettings(
    updateShowProfileBanner: (Boolean) -> Unit,
    settings: Settings,
    themeColors: ColorScheme
){
    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Profile Display",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = themeColors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ProfileBannerToggle(
                    title = "Show Profile Banner",
                    subtitle = "Display banner image on home screen",
                    isEnabled = settings.showProfileBanner,
                    onToggle = { updateShowProfileBanner(it) },
                    colors = themeColors
                )
            }
        }
    }
}

// Add this new composable function:
@Composable
fun ProfileBannerToggle(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: ColorScheme
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                fontFamily = iosFont,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.inverseSurface
            )
            Text(
                text = subtitle,
                textAlign = TextAlign.Start,
                fontFamily = iosFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = colors.inverseSurface.copy(0.6f)
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            modifier = Modifier.padding(start = 8.dp),
            thumbContent = null,
            enabled = true,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary,
                uncheckedThumbColor = colors.inverseSurface.copy(0.5f),
                uncheckedTrackColor = colors.background,
                uncheckedBorderColor = Color.Transparent
            ),
            interactionSource = remember { MutableInteractionSource() }
        )
    }
}


//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun AppearanceScreen(
//    onBackClicked: () -> Unit,
//    appearanceViewModel: AppearanceViewModel,
//    screenTitle: String,
//    previousScreenTitle: String
//) {
//    val settings by appearanceViewModel.settings.collectAsState()
//
//    val themeColors = MaterialTheme.colorScheme
//    val scrollOverScrollState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
//// Scaffold State for Scroll Behavior
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
//
//    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        topBar = {
//            CustomTitleTopAppBar(
//                scrollBehaviorLarge = scrollBehavior,
//                scrollBehaviorSmall =  scrollBehaviorSmall,
//                title = screenTitle,
//                previousScreenTitle = previousScreenTitle,
//                onBackClick = onBackClicked,
//                hasBackButton = true
//            )
//
//        },
//
//        content = { innerPadding ->
//            Box(modifier = Modifier
//                .fillMaxSize()
//            ){
//                LazyColumn(
//                    state = scrollOverScrollState,
//                    userScrollEnabled = false,
//                    modifier =
//                    Modifier
//                        .fillMaxSize()
//                        .padding(innerPadding)
//                        .padding(horizontal = 20.dp)
//                        .clip(RoundedCornerShape(16.dp))
//                        .overscroll(overscrollEffect)
//                        .scrollable(
//                            orientation = Orientation.Vertical,
//                            reverseDirection = true,
//                            state = scrollOverScrollState,
//                            overscrollEffect = overscrollEffect
//                        ),
//                    verticalArrangement = Arrangement.spacedBy(20.dp),
//                    horizontalAlignment = Alignment.Start
//                ) {
//                    item(key = "Accent Colors") {
//                        AccentColors(
//                            appearanceViewModel = appearanceViewModel,
//                            settings = settings,
//                            themeColors = themeColors
//                        )
//                    }
//                    item(key = "Navigation Bar Styles") {
//                        NavigationBarStyles(
//                            appearanceViewModel = appearanceViewModel,
//                            settings = settings,
//                            themeColors = themeColors
//                        )
//                    }
//                    item(key = "App Themes") {
//                        AppThemes(
//                            appearanceViewModel = appearanceViewModel,
//                            settings = settings,
//                            themeColors = themeColors
//                        )
//                    }
//                    item(key = "ProfileBanner In HomeScreen") {
//                        ProfileDisplaySettings(
//                            appearanceViewModel = appearanceViewModel,
//                            settings = settings,
//                            themeColors = themeColors
//                        )
//                    }
//                }
//                Spacer(
//                    modifier = Modifier
//                        .height(130.0.dp)
//                        .fillMaxWidth()
//                        .background(
//                            brush = Brush.verticalGradient(
//                                colors = listOf(
//                                    themeColors.background,
//                                    themeColors.background,
//                                    themeColors.background.copy(0.9f),
//                                    Color.Transparent
//                                )
//                            )
//                        )
//                        .align(Alignment.TopCenter)
//                )
//
//            }
//        }
//    )
//}
//
//@Composable
//fun AccentColors(
//    appearanceViewModel: AppearanceViewModel,
//    settings: Settings,
//    themeColors: ColorScheme
//){
//    Column(modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(10.dp),
//    ) {
//        Text(
//            text = "Accent Colors",
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Normal,
//            style = MaterialTheme.typography.bodyMedium,
//            color = themeColors.inversePrimary,
//            modifier = Modifier.padding(start = 5.dp)
//        )
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(themeColors.surface, shape = RoundedCornerShape(16.dp))
//        ) {
//            PrimaryColorPicker(
//                settings = settings,
//                isDarkTheme = isSystemInDarkTheme(),
//                initialColor = settings.primaryColor,
//                onPrimaryColorSelected = { selectedColor ->
//                    appearanceViewModel.updatePrimaryColor(selectedColor)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun NavigationBarStyles(
//    appearanceViewModel: AppearanceViewModel,
//    settings: Settings,
//    themeColors: ColorScheme
//){
//    Column(modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(10.dp),
//    ) {
//        Text(
//            text = "NavigationBar Styles",
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Normal,
//            style = MaterialTheme.typography.bodyMedium,
//            color = themeColors.inversePrimary,
//            modifier = Modifier.padding(start = 5.dp)
//        )
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    color = themeColors.surface,
//                    shape = RoundedCornerShape(16.dp)
//                )
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Box(modifier = Modifier.clickable {
//                appearanceViewModel.updateLabelVisibility(LabelVisibility.AlwaysShow)
//            }) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    AlwaysShowLabel(isSelected = settings.labelVisibility == LabelVisibility.AlwaysShow)
//                    Text(
//                        text = "Always Show",
//                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Medium,
//                        fontFamily = iosFont,
//                        fontSize = 10.sp,
//                        color = if (settings.labelVisibility == LabelVisibility.AlwaysShow) themeColors.primary
//                        else themeColors.inverseSurface.copy(0.5f)
//                    )
//                }
//            }
//
//            Box(modifier = Modifier.clickable {
//                appearanceViewModel.updateLabelVisibility(LabelVisibility.SelectedOnly)
//            }) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    SelectedOnlyLabel(isSelected = settings.labelVisibility == LabelVisibility.SelectedOnly)
//                    Text(
//                        text = "Selected Only",
//                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Medium,
//                        fontFamily = iosFont,
//                        fontSize = 10.sp,
//                        color = if (settings.labelVisibility == LabelVisibility.SelectedOnly) themeColors.primary
//                        else themeColors.inverseSurface.copy(0.5f)
//                    )
//                }
//            }
//
//            Box(modifier = Modifier.clickable {
//                appearanceViewModel.updateLabelVisibility(LabelVisibility.NeverShow)
//            }) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    NeverShowLabel(isSelected = settings.labelVisibility == LabelVisibility.NeverShow)
//                    Text(
//                        text = "Hide Labels",
//                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Medium,
//                        fontFamily = iosFont,
//                        fontSize = 10.sp,
//                        color = if (settings.labelVisibility == LabelVisibility.NeverShow) themeColors.primary
//                        else themeColors.inverseSurface.copy(0.5f)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AppThemes(
//    appearanceViewModel: AppearanceViewModel,
//    settings: Settings,
//    themeColors: ColorScheme
//){
//    Column(modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(10.dp),
//    ) {
//        Text(
//            text = "App Theme",
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Normal,
//            style = MaterialTheme.typography.bodyMedium,
//            color = themeColors.inversePrimary,
//            modifier = Modifier.padding(start = 5.dp)
//        )
//
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    color = themeColors.surface,
//                    shape = RoundedCornerShape(16.dp)
//                )
//        ) {
//            ThemeSelector(
//                currentTheme = settings.themeMode,
//                onThemeSelected = { selectedTheme ->
//                    appearanceViewModel.updateThemeMode(selectedTheme)
//                }
//            )
//        }
//    }
//}
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun ThemeSelector(
//    currentTheme: ThemeMode,
//    onThemeSelected: (ThemeMode) -> Unit
//) {
//    val themeColors = MaterialTheme.colorScheme
//
//    Column(
//        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(15.dp),
//        horizontalAlignment = Alignment.CenterHorizontally) {
//        LazyRow(
//            modifier = Modifier
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//
//            item {
//                ThemeOption(
//                    title = "Light Mode",
//                    isSelected = currentTheme == ThemeMode.Light,
//                    onClick = { onThemeSelected(ThemeMode.Light) },
//                    colors = themeColors,
//                    backgroundColorLight = Latte_Crust,
//                    surfaceColorLight = Latte_Base,
//                    backgroundColorDark = Latte_Crust,
//                    surfaceColorDark = Latte_Base
//                )
//            }
//
//            item {
//                ThemeOption(
//                    title = "Dark Mode",
//                    isSelected = currentTheme == ThemeMode.Dark,
//                    onClick = { onThemeSelected(ThemeMode.Dark) },
//                    colors = themeColors,
//                    backgroundColorLight = Macchiato_Crust,
//                    surfaceColorLight = Macchiato_Base,
//                    backgroundColorDark = Macchiato_Crust,
//                    surfaceColorDark = Macchiato_Base
//                )
//            }
//            item {
//                ThemeOption(
//                    title = "Black Amoled",
//                    isSelected = currentTheme == ThemeMode.Black,
//                    onClick = { onThemeSelected(ThemeMode.Black) },
//                    colors = themeColors,
//                    backgroundColorLight = Amoled_Crust,
//                    surfaceColorLight = Amoled_Base,
//                    backgroundColorDark = Amoled_Crust,
//                    surfaceColorDark = Amoled_Base
//                )
//            }
//        }
//        Column(verticalArrangement = Arrangement.spacedBy(5.dp),
//            horizontalAlignment = Alignment.CenterHorizontally) {
//            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color= MaterialTheme.colorScheme.inverseSurface.copy(0.2f))
//            SystemThemeOption(
//                title = "System Default",
//                isSelected = currentTheme == ThemeMode.System,
//                onClick = { onThemeSelected(ThemeMode.System) },
//                colors = themeColors
//            )
//            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color= MaterialTheme.colorScheme.inverseSurface.copy(0.2f))
//            SystemThemeOption(
//                title = "System Black",
//                isSelected = currentTheme == ThemeMode.SystemBlack,
//                onClick = { onThemeSelected(ThemeMode.SystemBlack) },
//                colors = themeColors
//            )
//        }
//    }
//
//}
//@Composable
//fun SystemThemeOption(
//    title: String,
//    isSelected: Boolean,
//    onClick: () -> Unit,
//    colors: ColorScheme
//){
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//            .clickable(onClick = onClick),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = title,
//            textAlign = TextAlign.Start,
//            fontFamily = iosFont,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium,
//            color = colors.inverseSurface
//        )
//        Switch(
//            checked = isSelected,
//            onCheckedChange = { onClick() },
//            modifier = Modifier.padding(start = 8.dp),
//            thumbContent = null,
//            enabled = true,
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = Color.White,
//                checkedTrackColor = colors.primary,
//                checkedBorderColor = colors.primary,
//                uncheckedThumbColor = colors.inverseSurface.copy(0.5f),
//                uncheckedTrackColor = colors.background,
//                uncheckedBorderColor = Color.Transparent
//            ),
//            interactionSource = remember { MutableInteractionSource() }
//        )
//    }
//}
//
//@Composable
//fun ThemeOption(
//    backgroundColorLight: Color,
//    surfaceColorLight: Color,
//    backgroundColorDark: Color,
//    surfaceColorDark: Color,
//    title: String,
//    isSelected: Boolean,
//    onClick: () -> Unit,
//    colors: ColorScheme
//){
//    Column(modifier = Modifier
//        .padding(horizontal = 16.dp)
//        .clickable(
//            onClick = onClick,
//            interactionSource = remember { MutableInteractionSource() },
//            indication = null
//        ),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Row(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).size(80.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Box(modifier = Modifier
//                .width(40.dp)
//                .height(80.dp)
//                .background(
//                    color = backgroundColorLight,
//                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
//                ),
//                contentAlignment = Alignment.Center
//            ){
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    Spacer(modifier = Modifier
//                        .padding(start = 10.dp)
//                        .height(10.dp)
//                        .width(30.dp)
//                        .background(
//                            color = surfaceColorLight,
//                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
//                        )
//                    )
//                    Spacer(modifier = Modifier
//                        .padding(start = 20.dp)
//                        .height(10.dp)
//                        .width(20.dp)
//                        .background(
//                            color = surfaceColorLight,
//                            shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
//                        )
//                    )
//                }
//            }
//            Box(modifier = Modifier
//                .width(40.dp)
//                .height(80.dp)
//                .background(
//                    color = backgroundColorDark,
//                    shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
//                ),
//                contentAlignment = Alignment.Center
//            ){
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    Spacer(modifier = Modifier
//                        .padding(end = 10.dp)
//                        .height(10.dp)
//                        .width(30.dp)
//                        .background(
//                            color = surfaceColorDark,
//                            shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
//                        )
//                    )
//                    Spacer(modifier = Modifier
//                        .padding(end = 20.dp)
//                        .height(10.dp)
//                        .width(20.dp)
//                        .background(
//                            color = surfaceColorDark,
//                            shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
//                        )
//                    )
//                }
//            }
//        }
//        Text(
//            text = title,
//            fontSize = 10.sp,
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Medium,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            textAlign = TextAlign.Center,
//            color = if (isSelected) colors.primary else colors.inverseSurface.copy(0.5f)
//        )
//
//    }
//}
//
//@Composable
//fun PrimaryColorPicker(
//    settings: Settings,
//    isDarkTheme: Boolean,
//    initialColor: Color,
//    onPrimaryColorSelected: (Color) -> Unit
//) {
//    val themeColors = MaterialTheme.colorScheme
//    var isCustomColorSelected by remember { mutableStateOf(false) }
//    var customColor by remember { mutableStateOf(initialColor) }
//    val controller = rememberColorPickerController()
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 10.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Toggle Button
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(15.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.custom_color_picker),
//                contentDescription = "Color Picker",
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape)
//                    .clickable { isCustomColorSelected = !isCustomColorSelected }
//                    .animateContentSize(), // Smooth size adjustment
//                tint = Color.Unspecified // Disable tinting
//            )
//
//            Spacer(Modifier
//                .width(1.dp)
//                .height(18.dp)
//                .background(themeColors.inverseSurface.copy(0.3f)))
//            // Accent Color Picker
//            AccentColorPicker(
//                themeMode = settings.themeMode,
//                isDarkTheme = isDarkTheme,
//                onColorSelected = { selectedColor ->
//                    customColor = selectedColor
//                    onPrimaryColorSelected(selectedColor)
//                }
//            )
//        }
//
//        // Conditional Content
//        AnimatedVisibility(visible = isCustomColorSelected) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 20.dp),
//                verticalArrangement = Arrangement.spacedBy(10.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // HSV Color Picker
//                HsvColorPicker(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(350.dp)
//                        .padding(30.dp),
//                    controller = controller,
//                    onColorChanged = { colorEnvelope: ColorEnvelope ->
//                        customColor = colorEnvelope.color
//                        onPrimaryColorSelected(colorEnvelope.color)
//                    },
//                    initialColor = initialColor
//                )
//                BrightnessSlider(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(20.dp)
//                        .height(35.dp),
//                    controller = controller
//                )
//
//                Button(
//                    onClick = {
//                        isCustomColorSelected = false
//                        onPrimaryColorSelected(customColor)
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    shape = RoundedCornerShape(10.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = themeColors.primary,
//                        contentColor = themeColors.inverseSurface
//                    )
//                ) {
//                    Text(
//                        text = "Set Color",
//                        fontFamily = iosFont,
//                        color = Color.White
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AccentColorPicker(
//    themeMode: ThemeMode = ThemeMode.System,
//    isDarkTheme: Boolean,
//    onColorSelected: (Color) -> Unit
//) {
//    val themeColors = MaterialTheme.colorScheme
//    val colors = when (themeMode){
//        ThemeMode.Dark -> listOf(
//            Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
//            Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
//            Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
//            Macchiato_Blue, Macchiato_Lavender
//        )
//        ThemeMode.Light -> listOf(
//            Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
//            Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
//            Latte_Teal, Latte_Green,
//        )
//
//        ThemeMode.Black -> listOf(
//            Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
//            Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
//            Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
//            Macchiato_Blue, Macchiato_Lavender
//        )
//
//        ThemeMode.System -> if (isDarkTheme) {
//            listOf(
//                Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
//                Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
//                Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
//                Macchiato_Blue, Macchiato_Lavender
//            )
//        } else {
//            listOf(
//                Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
//                Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
//                Latte_Teal, Latte_Green,
//            )
//        }
//
//        ThemeMode.SystemBlack -> if (isDarkTheme) {
//            listOf(
//                Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
//                Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
//                Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
//                Macchiato_Blue, Macchiato_Lavender
//            )
//        } else {
//            listOf(
//                Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
//                Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
//                Latte_Teal, Latte_Green,
//            )
//        }
//    }
//    Box(){
//        LazyRow(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 10.dp, vertical = 15.dp),
//            horizontalArrangement = Arrangement.spacedBy(20.dp)
//        ) {
//            colors.forEach { color ->
//                item{ Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .background(color)
//                        .clickable { onColorSelected(color) }
//                )}
//            }
//        }
//        Spacer(
//            modifier = Modifier
//                .padding(start = 6.dp)
//                .height(56.dp)
//                .width(10.dp)
//                .background(
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(
//                            themeColors.surface,
//                            Color.Transparent
//                        )
//                    )
//                )
//                .align(Alignment.TopStart)
//        )
//        Spacer(
//            modifier = Modifier
//                .padding(end = 6.dp)
//                .height(56.dp)
//                .width(10.dp)
//                .background(
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(
//                            Color.Transparent,
//                            themeColors.surface
//                        )
//                    )
//                )
//                .align(Alignment.TopEnd)
//        )
//
//    }
//}
//
//@Composable
//fun AlwaysShowLabel(isSelected: Boolean = false){
//
//    val themeColors = MaterialTheme.colorScheme
//    Box(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))){
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .background(color = themeColors.surfaceBright))
//        Box(
//            modifier = Modifier
//                .width(80.dp)
//                .height(60.dp)
//                .background(color = themeColors.background)
//                .align(Alignment.BottomCenter))
//        Box(
//            modifier = Modifier
//                .padding(top = 15.dp, bottom = 10.dp)
//                .size(16.dp)
//                .clip(RoundedCornerShape(5.dp))
//                .background(
//                    color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(
//                        0.3f
//                    )
//                )
//                .align(Alignment.Center))
//        Box(
//            modifier = Modifier
//                .width(80.dp)
//                .align(Alignment.BottomCenter)){
//            Text(
//                text = "Abc",
//                fontFamily = iosFont,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 12.sp,
//                textAlign = TextAlign.Center,
//                color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(0.3f),
//                modifier = Modifier.width(80.dp))
//        }
//    }
//
//}
//
//@Composable
//fun SelectedOnlyLabel(isSelected: Boolean = false){
//
//    val themeColors = MaterialTheme.colorScheme
//    Box(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))){
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .background(color = themeColors.surfaceBright))
//        Box(
//            modifier = Modifier
//                .width(80.dp)
//                .height(60.dp)
//                .background(color = themeColors.background)
//                .align(Alignment.BottomCenter))
//        Box(
//            modifier = Modifier
//                .padding(bottom = 23.dp, end = 10.dp)
//                .size(12.dp)
//                .clip(RoundedCornerShape(3.dp))
//                .background(
//                    color = if (isSelected) themeColors.primary.copy(0.5f) else themeColors.inverseSurface.copy(
//                        0.15f
//                    )
//                )
//                .align(Alignment.BottomEnd))
//        Box(
//            modifier = Modifier
//                .padding(bottom = 23.dp, start = 10.dp)
//                .size(12.dp)
//                .clip(RoundedCornerShape(3.dp))
//                .background(
//                    color = if (isSelected) themeColors.primary.copy(0.5f) else themeColors.inverseSurface.copy(
//                        0.15f
//                    )
//                )
//                .align(Alignment.BottomStart))
//        Box(
//            modifier = Modifier
//                .padding(top = 15.dp, bottom = 10.dp)
//                .size(16.dp)
//                .clip(RoundedCornerShape(5.dp))
//                .background(
//                    color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(
//                        0.3f
//                    )
//                )
//                .align(Alignment.Center))
//        Box(
//            modifier = Modifier
//                .width(80.dp)
//                .align(Alignment.BottomCenter)){
//            Text(
//                text = "Abc",
//                fontFamily = iosFont,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 12.sp,
//                textAlign = TextAlign.Center,
//                color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(0.3f),
//                modifier = Modifier.width(80.dp))
//        }
//    }
//
//}
//
//@Composable
//fun NeverShowLabel(isSelected: Boolean = false){
//
//    val themeColors = MaterialTheme.colorScheme
//    Box(modifier = Modifier.shadow(5.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))){
//        Box(
//            modifier = Modifier
//                .size(80.dp)
//                .background(color = themeColors.surfaceBright))
//        Box(
//            modifier = Modifier
//                .width(80.dp)
//                .height(60.dp)
//                .background(color = themeColors.background)
//                .align(Alignment.BottomCenter))
//        Box(
//            modifier = Modifier
//                .padding(top = 15.dp, bottom = 10.dp)
//                .size(16.dp)
//                .clip(RoundedCornerShape(5.dp))
//                .background(
//                    color = if (isSelected) themeColors.primary else themeColors.inverseSurface.copy(
//                        0.3f
//                    )
//                )
//                .align(Alignment.Center))
//    }
//
//}
//
//@Composable
//fun ProfileDisplaySettings(
//    appearanceViewModel: AppearanceViewModel,
//    settings: Settings,
//    themeColors: ColorScheme
//){
//    Column(modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(10.dp),
//    ) {
//        Text(
//            text = "Profile Display",
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Normal,
//            style = MaterialTheme.typography.bodyMedium,
//            color = themeColors.inversePrimary,
//            modifier = Modifier.padding(start = 5.dp)
//        )
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    color = themeColors.surface,
//                    shape = RoundedCornerShape(16.dp)
//                )
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(5.dp)
//            ) {
//                ProfileBannerToggle(
//                    title = "Show Profile Banner",
//                    subtitle = "Display banner image on home screen",
//                    isEnabled = settings.showProfileBanner,
//                    onToggle = { appearanceViewModel.updateShowProfileBanner(it) },
//                    colors = themeColors
//                )
//            }
//        }
//    }
//}
//
//// Add this new composable function:
//@Composable
//fun ProfileBannerToggle(
//    title: String,
//    subtitle: String,
//    isEnabled: Boolean,
//    onToggle: (Boolean) -> Unit,
//    colors: ColorScheme
//){
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onToggle(!isEnabled) },
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = title,
//                textAlign = TextAlign.Start,
//                fontFamily = iosFont,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                color = colors.inverseSurface
//            )
//            Text(
//                text = subtitle,
//                textAlign = TextAlign.Start,
//                fontFamily = iosFont,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.Normal,
//                color = colors.inverseSurface.copy(0.6f)
//            )
//        }
//        Switch(
//            checked = isEnabled,
//            onCheckedChange = onToggle,
//            modifier = Modifier.padding(start = 8.dp),
//            thumbContent = null,
//            enabled = true,
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = Color.White,
//                checkedTrackColor = colors.primary,
//                checkedBorderColor = colors.primary,
//                uncheckedThumbColor = colors.inverseSurface.copy(0.5f),
//                uncheckedTrackColor = colors.background,
//                uncheckedBorderColor = Color.Transparent
//            ),
//            interactionSource = remember { MutableInteractionSource() }
//        )
//    }
//}
