package com.ritesh.cashiro.presentation.ui.extras.components.profile

import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingEvent
import com.ritesh.cashiro.presentation.ui.features.profile.EditProfileState
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenEvent
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    state: ProfileScreenState,
    sheetState: SheetState,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    onEvent: (ProfileScreenEvent) -> Unit,
    profilePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    bannerPhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    requestStoragePermission: () -> Unit,
) {
    val editState = state.editState
    val themeColors = MaterialTheme.colorScheme

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            onEvent(ProfileScreenEvent.DismissEditSheet)
        },
        sheetMaxWidth = usedScreenWidth - 10.dp,
        containerColor = themeColors.background,
        contentColor = themeColors.inverseSurface,
        dragHandle = {
            DragHandle(
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        ProfileSheetContent(
            state = state,
            requestStoragePermission = requestStoragePermission,
            editState = editState,
            bannerPhotoPicker = bannerPhotoPicker,
            profilePhotoPicker = profilePhotoPicker,
            onEvent = onEvent,
            themeColors = themeColors,
            modifier = Modifier.height(usedScreenHeight)
        )
    }
}
@Composable
fun ProfileSheetContent(
    state: ProfileScreenState,
    requestStoragePermission: () -> Unit,
    editState: EditProfileState,
    bannerPhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    profilePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    onEvent: (ProfileScreenEvent) -> Unit,
    themeColors: ColorScheme,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column {
            LazyColumn(modifier = Modifier.clip(RoundedCornerShape(16.dp)).weight(1f).padding(horizontal = 16.dp)) {
                item(key = "Header text") {
                    Text(
                        text = "Edit Profile",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = iosFont,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp)
                    )
                }
                item(key = "Profile Card Preview") {
                    ProfileCardPreview(
                        hasStoragePermission = state.hasStoragePermission,
                        requestStoragePermission = requestStoragePermission,
                        selectedBannerBackgroundUri = editState.editedBannerImageUri,
                        bannerPhotoPicker = bannerPhotoPicker,
                        selectedProfileImageUri = editState.editedProfileImageUri,
                        profilePhotoPicker = profilePhotoPicker,
                        profileBgColor = editState.editedProfileBackgroundColor,
                        themeColors = themeColors,
                    )
                }
                item(key = "User Name Input") {
                    UserNameInput(
                        userName = editState.editedUserName,
                        onUserNameChange = { newName ->
                            onEvent(ProfileScreenEvent.UpdateEditUserName(newName))
                        },
                        themeColors = themeColors
                    )
                }
                item(key = "Profile Images") {
                    ProfileImageSelection(
                        themeColors = themeColors,
                        profilePhotoPicker = profilePhotoPicker,
                        setSelectedProfileImageUri = { uri ->
                            onEvent(ProfileScreenEvent.UpdateEditProfileImage(uri))
                        }
                    )
                }
                item(key = "Profile Background Colors") {
                    ProfileBackgroundColorSelection(
                        themeColors = themeColors,
                        profileBgColor = editState.editedProfileBackgroundColor,
                        onColorSelected = { color ->
                            onEvent(ProfileScreenEvent.UpdateEditProfileBackgroundColor(color))
                        }
                    )
                }
            }
            Button(
                onClick = {
                    onEvent(ProfileScreenEvent.SaveProfileChanges)
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (editState.hasChanges) {
                        themeColors.primary
                    } else {
                        themeColors.surfaceVariant
                    },
                    contentColor = if (editState.hasChanges) {
                        Color.White
                    } else {
                        themeColors.onSurfaceVariant
                    }
                ),
                enabled = editState.hasChanges
            ) {
                Text(
                    text = "Save Changes",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
@Composable
private fun ProfileCardPreview(
    hasStoragePermission: Boolean,
    requestStoragePermission: () -> Unit,
    selectedBannerBackgroundUri: Uri?,
    bannerPhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    selectedProfileImageUri: Uri?,
    profilePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    profileBgColor: Color,
    showBanner: Boolean = true,
    themeColors: ColorScheme
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 15.dp), contentAlignment = Alignment.Center) {

        AnimatedVisibility( visible = showBanner) {
            BannerBackgroundSelector(
                hasStoragePermission = hasStoragePermission,
                requestStoragePermission = requestStoragePermission,
                selectedBannerBackgroundUri = selectedBannerBackgroundUri,
                singleBannerBackgroundPhotoPickerLauncher = bannerPhotoPicker
            )
        }

        // Fade Bottom Edge effect for Banner
        Spacer(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeColors.background.copy(0.5f),
                            themeColors.background
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )
        ProfilePhotoSelector(
            selectedProfileImageUri = selectedProfileImageUri,
            singleProfilePhotoPickerLauncher = profilePhotoPicker,
            bgColor = profileBgColor,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun UserNameInput(
    userName: String,
    onUserNameChange: (String) -> Unit,
    themeColors: ColorScheme,
) {
    Column(modifier = Modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Name",
            fontSize = 14.sp,
            fontFamily = iosFont,
            color = themeColors.inverseSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 10.dp, top = 20.dp, bottom = 5.dp)
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            placeholder = {
                Text(
                    text = "Name",
                    style = TextStyle(
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inverseSurface.copy(0.5f)
                    ),
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(),
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.edit_name_bulk),
                    tint = themeColors.inverseSurface.copy(0.5f),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    themeColors.surface,
                    RoundedCornerShape(15.dp)
                ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ProfileBackgroundColorSelection(
    profileBgColor: Color,
    onColorSelected: (Color) -> Unit,
    themeColors: ColorScheme,
) {
    Column(modifier = Modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Background Colors",
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.inverseSurface,
            modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 5.dp)
        )
        ProfileBgColorPicker(
            initialColor = profileBgColor,
            onPrimaryColorSelected = onColorSelected,
            themeColors = themeColors
        )
    }
}
@Composable
private fun ProfileImageSelection(
    profilePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    setSelectedProfileImageUri: (Uri?) -> Unit,
    themeColors: ColorScheme,
){
    Column(modifier = Modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Profile Photo",
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.inverseSurface,
            modifier = Modifier.padding(start = 10.dp, top = 20.dp, bottom = 5.dp)
        )
        ProfilePhotoPicker(
            initialPhoto = R.drawable.avatar_1,
            themeColors = themeColors,
            singleProfilePhotoPickerLauncher = profilePhotoPicker,
            onPrimaryPhotoSelected = { },
            setProfileImageUri = { uri ->
                setSelectedProfileImageUri(uri)
            }
        )
    }
}

@Composable
private fun BannerBackgroundSelector(
    hasStoragePermission: Boolean,
    requestStoragePermission: () -> Unit,
    selectedBannerBackgroundUri: Uri?,
    singleBannerBackgroundPhotoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
){
    Box(
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(
                onClick = {
                    if (hasStoragePermission) {
                        // Use ImageOnly which includes GIFs
                        singleBannerBackgroundPhotoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    } else {
                        requestStoragePermission()
                    }
                }
            )
    ){
        if (selectedBannerBackgroundUri != null) {
        // Determine if the URI is a GIF by checking its extension or MIME type
            val isGif = remember(selectedBannerBackgroundUri) {
                selectedBannerBackgroundUri.toString().endsWith(".gif", ignoreCase = true)
            }
            if (isGif) {
                val context = LocalContext.current

                // Set up the image loader with GIF support once
                val imageLoader = remember {
                    ImageLoader.Builder(context)
                        .components {
                            // For Coil 2.6.0, we need to add the GIF decoders this way
                            if (SDK_INT >= 28) {
                                add(AnimatedImageDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }
                        .build()
                }
                // Create the image request for the URI
                val imageRequest = ImageRequest.Builder(context)
                    .data(selectedBannerBackgroundUri)
                    .crossfade(true)
                    .build()

                // Use AsyncImage with the configured loader that supports GIFs
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Banner Background",
                    contentScale = ContentScale.Crop,
                    imageLoader = imageLoader
                )
            } else {
                AsyncImage(
                    model = selectedBannerBackgroundUri,
                    contentDescription = "Banner Background",
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // Default background image when nothing is selected
            AsyncImage(
                model = R.drawable.banner_bg_image,
                contentDescription = "Default Banner Background",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ProfilePhotoSelector(
    selectedProfileImageUri: Uri?,
    singleProfilePhotoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    bgColor: Color,
    modifier: Modifier = Modifier
){

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.BottomCenter
    ){
        Box(
            modifier =  Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(bgColor)
                .clickable(
                    onClick = {
                        singleProfilePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
        ) {

            if (selectedProfileImageUri != null ){
                val isGif = remember(selectedProfileImageUri) {
                    selectedProfileImageUri.toString().endsWith(".gif", ignoreCase = true)
                }

                if (isGif) {
                    val context = LocalContext.current

                    // Set up the image loader with GIF support once
                    val imageLoader = remember {
                        ImageLoader.Builder(context)
                            .components {
                                // For Coil 2.6.0, we need to add the GIF decoders this way
                                if (SDK_INT >= 28) {
                                    add(AnimatedImageDecoder.Factory())
                                } else {
                                    add(GifDecoder.Factory())
                                }
                            }
                            .build()
                    }
                    val imageRequest = ImageRequest.Builder(context)
                        .data(selectedProfileImageUri)
                        .crossfade(true)
                        .build()
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )

                } else{
                    AsyncImage(
                        model = selectedProfileImageUri,
                        contentDescription = "Banner Background",
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                AsyncImage(
                    model = R.drawable.avatar_1,
                    contentDescription = "Default Profile Image",
                    contentScale = ContentScale.Crop
                )
            }

        }

        IconButton(
            onClick = {
                singleProfilePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(15.dp)
                )
                .size(40.dp)
                .align(Alignment.BottomEnd),
        ) {
            Icon(
                painter = painterResource(R.drawable.edit_bulk),
                contentDescription = null,
                tint =  Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileBgColorPicker(
    initialColor: Color,
    onPrimaryColorSelected: (Color) -> Unit,
    themeColors:  ColorScheme,
){

    var isCustomColorSelected by remember { mutableStateOf(false) }
    var customColor by remember { mutableStateOf(initialColor) }
    val controller = rememberColorPickerController()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.surface, RoundedCornerShape(15.dp))
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
            ProfileBgPresetColors(
                onColorSelected = { selectedColor ->
                    customColor = selectedColor
                    onPrimaryColorSelected(selectedColor)
                },
                themeColors = themeColors
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
private fun ProfileBgPresetColors(
    onColorSelected: (Color) -> Unit,
    themeColors:  ColorScheme
){
    val presetColors = listOf<Color>(
        Macchiato_Rosewater, Macchiato_Flamingo, Macchiato_Pink, Macchiato_Mauve,
        Macchiato_Red, Macchiato_Maroon, Macchiato_Peach, Macchiato_Yellow,
        Macchiato_Green, Macchiato_Teal, Macchiato_Sky, Macchiato_Sapphire,
        Macchiato_Blue, Macchiato_Lavender,Latte_Blue,Latte_Rosewater, Latte_Flamingo, Latte_Pink, Latte_Mauve,
        Latte_Red, Latte_Maroon, Latte_Peach, Latte_Yellow,
        Latte_Teal, Latte_Green,
    )
    Box(){
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            presetColors.forEach { color ->
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
private fun ProfilePhotoPicker(
    initialPhoto: Int,
    onPrimaryPhotoSelected: (Int) -> Unit,
    singleProfilePhotoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    themeColors:  ColorScheme,
    setProfileImageUri: (Uri?) -> Unit
){

    var customPhoto by remember { mutableIntStateOf(initialPhoto) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.surface, RoundedCornerShape(15.dp))
            .padding(horizontal = 16.dp),
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
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Photo Picker",
                modifier = Modifier
                    .size(35.dp)
                    .clickable (
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            singleProfilePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    .animateContentSize(),
                tint = themeColors.inverseSurface.copy(0.5f)
            )

            Spacer(Modifier
                .width(1.dp)
                .height(30.dp)
                .background(themeColors.inverseSurface.copy(0.3f)))
            ProfilePresetPhotos(
                onPhotoSelected = { selectedPhoto ->
                    customPhoto = selectedPhoto
                    onPrimaryPhotoSelected(selectedPhoto)
                },
                setProfileImageUri = setProfileImageUri,
                themeColors = themeColors
            )
        }
    }
}
@Composable
private fun ProfilePresetPhotos(
    onPhotoSelected: (Int) -> Unit,
    setProfileImageUri: (Uri?) -> Unit,
    context: Context = LocalContext.current,
    themeColors:  ColorScheme
){
    val presetPhotos = listOf(
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6,
        R.drawable.avatar_7,
        R.drawable.avatar_8,
        R.drawable.avatar_9,
        R.drawable.avatar_10,
    )
    Box(){
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            presetPhotos.forEach { photo ->
                item{
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable {
                                onPhotoSelected(photo)
                                val resourceUri = Uri.parse("android.resource://${context.packageName}/$photo")
                                setProfileImageUri(resourceUri)
                            }
                    ){
                        Image(
                            painter = painterResource(id = photo),
                            contentDescription = null
                        )
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .padding(start = 6.dp)
                .height(106.dp)
                .width(20.dp)
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
                .height(106.dp)
                .width(20.dp)
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
fun CreateProfileOnBoardingContent(
    state: ProfileScreenState,
    requestStoragePermission: () -> Unit,
    editState: EditProfileState,
    bannerPhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    profilePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    onEvent: (ProfileScreenEvent) -> Unit,
    onNext: () -> Unit,
    themeColors: ColorScheme,
    onOnBoardingEvent: ((OnBoardingEvent) -> Unit)? = null,
    modifier: Modifier = Modifier
){
    // Check if user has made minimum required changes
    val hasMinimumData = editState.editedUserName.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxSize() // Add space for button
        ) {
            item(key = "Header text") {
                Text(
                    text = "Create Your Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
                )
            }
            item(key = "Profile Card Preview") {
                ProfileCardPreview(
                    hasStoragePermission = state.hasStoragePermission,
                    requestStoragePermission = requestStoragePermission,
                    selectedBannerBackgroundUri = editState.editedBannerImageUri,
                    bannerPhotoPicker = bannerPhotoPicker,
                    showBanner = false,
                    selectedProfileImageUri = editState.editedProfileImageUri,
                    profilePhotoPicker = profilePhotoPicker,
                    profileBgColor = editState.editedProfileBackgroundColor,
                    themeColors = themeColors,
                )
            }
            item(key = "User Name Input") {
                UserNameInput(
                    userName = editState.editedUserName,
                    onUserNameChange = { newName ->
                        onEvent(ProfileScreenEvent.UpdateEditUserName(newName))
                        // Sync with onboarding state
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateUserName(newName))
                    },
                    themeColors = themeColors
                )
            }
            item(key = "Profile Images") {
                ProfileImageSelection(
                    themeColors = themeColors,
                    profilePhotoPicker = profilePhotoPicker,
                    setSelectedProfileImageUri = { uri ->
                        onEvent(ProfileScreenEvent.UpdateEditProfileImage(uri))
                        // Sync with onboarding state
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateProfileImage(uri))
                    }
                )
            }
            item(key = "Profile Background Colors") {
                ProfileBackgroundColorSelection(
                    themeColors = themeColors,
                    profileBgColor = editState.editedProfileBackgroundColor,
                    onColorSelected = { color ->
                        onEvent(ProfileScreenEvent.UpdateEditProfileBackgroundColor(color))
                        // Sync with onboarding state
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateProfileBackgroundColor(color))
                    }
                )
            }
            item(key = "Continue Button"){
                // Fixed Continue Button
                Button(
                    onClick = {
                        // Save profile changes first
                        onEvent(ProfileScreenEvent.SaveProfileChanges)
                        // Sync final state to onboarding
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateUserName(editState.editedUserName))
                        editState.editedProfileImageUri?.let { uri ->
                            onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateProfileImage(uri))
                        }
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateProfileBackgroundColor(editState.editedProfileBackgroundColor))
                        // Then proceed to next step
                        onNext()
                    },
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = if (hasMinimumData) {
                                themeColors.primary
                            } else {
                                themeColors.surface
                            },
                            ambientColor = Color.Transparent
                        )
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasMinimumData) {
                            themeColors.primary
                        } else {
                            themeColors.surface
                        },
                        contentColor = if (hasMinimumData) {
                            Color.White
                        } else {
                            themeColors.inverseSurface.copy(0.5f)
                        }
                    ),
                    enabled = hasMinimumData
                ) {
                    Text(
                        text = "Continue",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}