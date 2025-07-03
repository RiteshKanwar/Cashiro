package com.ritesh.cashiro.presentation.ui.features.onboarding

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.result.PickVisualMediaRequest
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.domain.utils.PermissionUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.presentation.ui.extras.components.account.CreateAccountOnBoardingContent
import com.ritesh.cashiro.presentation.ui.extras.components.profile.CreateProfileOnBoardingContent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.profile.EditProfileState
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenEvent
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenState
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenViewModel
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import kotlin.math.PI
import kotlin.math.sin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingScreen(
    onBoardingEvent: (OnBoardingEvent) -> Unit,
    onBoardingState: OnBoardingState,
    onProfileEvent: (ProfileScreenEvent) -> Unit,
    profileUiState: ProfileScreenState,
    accountUiState: AccountScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    selectCurrency: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editState = profileUiState.editState
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onProfileEvent(ProfileScreenEvent.UpdateEditProfileImage(uri))
            }
        }
    )

    val bannerPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onProfileEvent(ProfileScreenEvent.UpdateEditBannerImage(uri))
            }
        }
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeColors = MaterialTheme.colorScheme

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    val accountName = accountUiState.accountName
    val balance = accountUiState.balance
    val cardColor1 = accountUiState.cardColor1
    val cardColor2 = accountUiState.cardColor2
    val account = accountUiState.accounts
    val currencyCode = accountUiState.currencyCode
    val isMainAccount = accountUiState.isMainAccount

    val accountNameState = remember{ mutableStateOf(accountName) }
    val balanceState = remember { mutableStateOf(balance) }
    val isMainAccountState = remember { mutableStateOf(isMainAccount) }
    val cardColor1State = remember { mutableStateOf(cardColor1 ?: Color.White) }
    val cardColor2State = remember { mutableStateOf(cardColor2 ?: Color.Black) }
    val currencyCodeState = remember { mutableStateOf(currencyCode) }

    val specialKeys = setOf('+', '-', '*', '/', '(', ')', '%', '×', '÷')

    val balanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    val accountBalanceSheetOpen = rememberSaveable { mutableStateOf(false) }
    val showCurrencySheet = rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    var isSheetOpen = rememberSaveable { mutableStateOf(false) }

    val firstColorsList = remember {
        listOf(
            Color(0xFF696eff),
            Color(0xFF6420AA),
            Color(0xFF83C6A4),
            Color(0xFFff0f7b),
            Color(0xFFff930f),
            Color(0xFFf9b16e)
        ) }
    val secondColorsList = remember {
        listOf(
            Color(0xFF93D7DE),
            Color(0xFFFF3EA5),
            Color(0xFFf8acff),
            Color(0xFFf89b29),
            Color(0xFFfff95b),
            Color(0xFFf68080),
        ) }
    val cardFirstColorIndex = rememberSaveable { mutableIntStateOf(0) }
    val cardSecondColorIndex = rememberSaveable { mutableIntStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val lazyListState = rememberLazyListState()
    val showLeftSpacer = lazyListState.firstVisibleItemIndex > 0
    val showRightSpacer = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != firstColorsList.lastIndex

    // Permission launchers
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onBoardingEvent(OnBoardingEvent.UpdateNotificationPermission(isGranted))
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        onBoardingEvent(OnBoardingEvent.UpdateStoragePermission(granted))
    }

    // Handle device back button/gesture navigation
    val canGoBack = onBoardingState.currentStep != OnBoardingStep.WELCOME &&
            onBoardingState.currentStep != OnBoardingStep.COMPLETION

    BackHandler(enabled = canGoBack) {
        onBoardingEvent(OnBoardingEvent.PreviousStep)
    }

    // Fetch All injected categories and subCategories
    LaunchedEffect(Unit) {
        onCategoryEvent(CategoryScreenEvent.FetchAllCategories)
        onCategoryEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
    }
    // Handle completion
    LaunchedEffect(onBoardingState.isOnBoardingComplete) {
        if (onBoardingState.isOnBoardingComplete) {
            onComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Progress indicator
            OnBoardingProgressIndicator(
                currentStep = onBoardingState.currentStep,
                modifier = Modifier.padding(top = 26.dp)
            )

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = onBoardingState.currentStep,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn() togetherWith slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeOut()
                    },
                    label = "onboarding_content"
                ) { step ->
                    when (step) {
                        OnBoardingStep.WELCOME -> WelcomeStep(
                            onNext = { onBoardingEvent(OnBoardingEvent.NextStep) },
                            onSkipAll = { onBoardingEvent(OnBoardingEvent.CompleteOnBoarding) }
                        )
                        OnBoardingStep.NOTIFICATION_PERMISSION -> NotificationPermissionStep(
                            hasPermission = onBoardingState.hasNotificationPermission,
                            onRequestPermission = {
                                onBoardingEvent(OnBoardingEvent.RequestNotificationPermission)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onBoardingEvent(OnBoardingEvent.UpdateNotificationPermission(true))
                                }
                            },
                            onSkip = { onBoardingEvent(OnBoardingEvent.NextStep) }
                        )
                        OnBoardingStep.STORAGE_PERMISSION -> StoragePermissionStep(
                            hasPermission = onBoardingState.hasStoragePermission,
                            onRequestPermission = {
                                onBoardingEvent(OnBoardingEvent.RequestStoragePermission)
                                storagePermissionLauncher.launch(PermissionUtils.getRequiredStoragePermissions())
                            },
                            onSkip = { onBoardingEvent(OnBoardingEvent.NextStep) }
                        )
                        OnBoardingStep.USER_PROFILE -> UserProfileStep(
                            onNext = { onBoardingEvent(OnBoardingEvent.NextStep) },
                            profileState = profileUiState,
                            requestStoragePermission = {
                                onBoardingEvent(OnBoardingEvent.RequestStoragePermission)
                                storagePermissionLauncher.launch(PermissionUtils.getRequiredStoragePermissions())
                            },
                            editState = editState,
                            profilePhotoPicker = profilePhotoPicker,
                            bannerPhotoPicker = bannerPhotoPicker,
                            onEvent = onProfileEvent,
                            themeColors = MaterialTheme.colorScheme,
                            onOnBoardingEvent = onBoardingEvent
                        )
                        OnBoardingStep.FIRST_ACCOUNT -> FirstAccountStep(
                            accountName = onBoardingState.accountName,
                            onNext = { onBoardingEvent(OnBoardingEvent.NextStep) },
                            specialKeys = specialKeys,
                            balanceState = balanceState,
                            accountNameState = accountNameState,
                            currencyCodeState = currencyCodeState,
                            cardColor1State = cardColor1State,
                            cardColor2State = cardColor2State,
                            currencySheetState = currencySheetState,
                            balanceSheetState = balanceSheetState,
                            infiniteTransition = infiniteTransition,
                            lazyListState = lazyListState,
                            firstColorsList = firstColorsList,
                            secondColorsList = secondColorsList,
                            cardFirstColorIndex = cardFirstColorIndex,
                            cardSecondColorIndex = cardSecondColorIndex,
                            isMainAccountState = isMainAccountState,
                            accountBalanceSheetOpen = accountBalanceSheetOpen,
                            showCurrencySheet = showCurrencySheet,
                            showLeftSpacer = showLeftSpacer,
                            showRightSpacer = showRightSpacer,
                            usedScreenWidth = usedScreenWidth,
                            usedScreenHeight = usedScreenHeight,
                            themeColors = themeColors,
                            onOnBoardingEvent = onBoardingEvent,
                            accountUiState = accountUiState,
                            onAccountEvent = onAccountEvent,
                            updateAccountCurrency = updateAccountCurrency,
                            currencyUiState = currencyUiState,
                            selectCurrency = selectCurrency,
                            onAddTransactionEvent = onAddTransactionEvent,
                            transactionUiState = transactionUiState,
                        )
                        OnBoardingStep.COMPLETION -> CompletionStep(
                            onFinish = { onBoardingEvent(OnBoardingEvent.CompleteOnBoarding) },
                            isLoading = onBoardingState.isLoading
                        )
                    }
                }
            }

            // REMOVED: OnBoardingNavigationButtons call
            // Navigation is now handled by device back button/gesture via BackHandler above
        }

        // Error snackbar
        onBoardingState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error somehow - you might want to use a SnackbarHost
                onBoardingEvent(OnBoardingEvent.ClearError)
            }
        }
    }
}
@Composable
private fun OnBoardingProgressIndicator(
    currentStep: OnBoardingStep,
    modifier: Modifier = Modifier
) {
    val steps = OnBoardingStep.entries.toTypedArray()
    val currentIndex = steps.indexOf(currentStep)
    val progress = (currentIndex + 1).toFloat() / steps.size

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Step ${currentIndex + 1} of ${steps.size}",
            fontSize = 12.sp,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
private fun WelcomeStep(
    onNext: () -> Unit,
    onSkipAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome animation
        val infiniteTransition = rememberInfiniteTransition(label = "welcome")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
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
                        color = MaterialTheme.colorScheme.surfaceBright.copy(0.5f),
                        shape = CircleShape
                    ),
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceBright,
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

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to Cashiro!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Let's get you set up with your personal finance companion. This will only take a few minutes.",
            fontSize = 16.sp,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = MaterialTheme.colorScheme.primary,
                    ambientColor = Color.Transparent
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = iosFont
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onSkipAll,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip Setup",
                fontSize = 14.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun NotificationPermissionStep(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSkip: () -> Unit
) {
    // Shake animation with pause
    val infiniteTransition = rememberInfiniteTransition(label = "shake")

    // Total cycle duration: 1.5s shake + 1s pause = 2.5s
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500, // Total cycle duration
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "animation_progress"
    )

    // Calculate shake values based on progress
    // Shake happens in first 1.5s (0f to 0.6f), pause happens in last 1s (0.6f to 1f)
    val isShaking = animationProgress <= 0.6f
    val shakePhase = if (isShaking) (animationProgress / 0.6f) else 0f

    val shakeOffset = if (isShaking) {
        sin(shakePhase * 8 * PI).toFloat() * 8f // 8 oscillations during shake period
    } else 0f

    val shakeRotation = if (isShaking) {
        sin(shakePhase * 6 * PI).toFloat() * 3f // 6 oscillations during shake period
    } else 0f

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.type_event_and_place_bell),
            contentDescription = "Notifications",
            modifier = Modifier
                .size(80.dp)
                .offset(x = shakeOffset.dp)
                .rotate(shakeRotation),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enable Notifications",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Get reminded to track your expenses and never miss important financial updates.",
            fontSize = 16.sp,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (hasPermission) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.verify_bulk),
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notifications Enabled",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = MaterialTheme.colorScheme.primary,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Enable Notifications",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip for now",
                fontSize = 14.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f)
            )
        }
    }
}
@Composable
private fun StoragePermissionStep(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSkip: () -> Unit
) {
    // Floating and breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "storage_animation")

    // Gentle floating animation (vertical movement)
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    // Breathing animation (scale)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    // Subtle rotation for added life
    val subtleRotation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtle_rotation"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.type_stationary_card_file_box),
            contentDescription = "Storage",
            modifier = Modifier
                .size(80.dp)
                .offset(y = floatingOffset.dp)
                .scale(breathingScale)
                .rotate(subtleRotation),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Storage Access",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Allow access to your device storage to upload profile photos and export your data.",
            fontSize = 16.sp,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (hasPermission) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.verify_bulk),
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Storage Access Granted",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = MaterialTheme.colorScheme.primary,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Grant Storage Access",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip for now",
                fontSize = 14.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun UserProfileStep(
    onNext: () -> Unit,
    profileState: ProfileScreenState,
    requestStoragePermission: () -> Unit,
    editState: EditProfileState,
    bannerPhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    profilePhotoPicker: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    onEvent: (ProfileScreenEvent) -> Unit,
    onOnBoardingEvent: (OnBoardingEvent) -> Unit,
    themeColors: ColorScheme,
) {

        CreateProfileOnBoardingContent(
            state = profileState,
            requestStoragePermission = requestStoragePermission,
            editState = editState,
            bannerPhotoPicker = bannerPhotoPicker,
            profilePhotoPicker = profilePhotoPicker,
            onEvent = onEvent,
            themeColors = themeColors,
            onNext = onNext,
            onOnBoardingEvent = onOnBoardingEvent,
        )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstAccountStep(
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onOnBoardingEvent: (OnBoardingEvent) -> Unit,
    currentAccountEntity: AccountEntity? = null,
    specialKeys: Set<Char>,
    accountName: String,
    balanceState: MutableState<String>,
    accountNameState: MutableState<String>,
    currencyCodeState: MutableState<String>,
    cardColor1State: MutableState<Color>,
    cardColor2State: MutableState<Color>,
    currencySheetState: SheetState,
    balanceSheetState: SheetState,
    infiniteTransition: InfiniteTransition,
    lazyListState: LazyListState,
    firstColorsList: List<Color>,
    secondColorsList: List<Color>,
    cardFirstColorIndex: MutableIntState,
    cardSecondColorIndex: MutableIntState,
    isMainAccountState: MutableState<Boolean>,
    accountBalanceSheetOpen: MutableState<Boolean>,
    showCurrencySheet: MutableState<Boolean>,
    onNext: () -> Unit,
    isUpdate: Boolean = false,
    showLeftSpacer: Boolean,
    showRightSpacer: Boolean,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme,
) {

    CreateAccountOnBoardingContent(
        currentAccountEntity = currentAccountEntity,
        specialKeys = specialKeys,
        accountName = accountName,
        balanceState = balanceState,
        accountNameState = accountNameState,
        currencyCodeState = currencyCodeState,
        cardColor1State = cardColor1State,
        cardColor2State = cardColor2State,
        currencySheetState = currencySheetState,
        balanceSheetState = balanceSheetState,
        infiniteTransition = infiniteTransition,
        lazyListState = lazyListState,
        firstColorsList = firstColorsList,
        secondColorsList = secondColorsList,
        cardFirstColorIndex = cardFirstColorIndex,
        cardSecondColorIndex = cardSecondColorIndex,
        isMainAccountState = isMainAccountState,
        accountBalanceSheetOpen = accountBalanceSheetOpen,
        showCurrencySheet = showCurrencySheet,
        onNext = onNext,
        onOnBoardingEvent = onOnBoardingEvent,
        isUpdate = isUpdate,
        showLeftSpacer = showLeftSpacer,
        showRightSpacer = showRightSpacer,
        usedScreenWidth = usedScreenWidth,
        usedScreenHeight = usedScreenHeight,
        themeColors = themeColors,
        accountUiState = accountUiState,
        onAccountEvent = onAccountEvent,
        updateAccountCurrency = updateAccountCurrency,
        currencyUiState = currencyUiState,
        selectCurrency = selectCurrency,
        onAddTransactionEvent = onAddTransactionEvent,
        transactionUiState = transactionUiState,
    )

}
@Composable
private fun CompletionStep(
    onFinish: () -> Unit,
    isLoading: Boolean = false // Add this parameter
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            // Show loading state while setting up default data
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Setting Up Your App...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We're preparing your categories and getting everything ready for you.",
                fontSize = 16.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        } else {
            // Success animation - existing code
            val infiniteTransition = rememberInfiniteTransition(label = "success")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Icon(
                painter = painterResource(R.drawable.verify_bulk),
                contentDescription = "Success",
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "You're All Set!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cashiro is ready to help you take control of your finances. Start tracking your expenses and achieve your financial goals!",
                fontSize = 16.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = MaterialTheme.colorScheme.primary,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Start Using Cashiro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont
                )
            }
        }
    }
}
fun validateOnBoardingStep(
    step: OnBoardingStep,
    profileState: ProfileScreenState? = null,
    accountNameState: String? = null,
    balanceState: String? = null
): Boolean {
    return when (step) {
        OnBoardingStep.USER_PROFILE -> {
            profileState?.editState?.editedUserName?.isNotBlank() == true
        }
        OnBoardingStep.FIRST_ACCOUNT -> {
            !accountNameState.isNullOrBlank() &&
                    !balanceState.isNullOrBlank() &&
                    balanceState != "0"
        }
        else -> true
    }
}