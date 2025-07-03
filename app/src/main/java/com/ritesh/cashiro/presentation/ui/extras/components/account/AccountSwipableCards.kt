package com.ritesh.cashiro.presentation.ui.extras.components.account
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.navigation.ACCOUNT_CARD_KEY_PREFIX
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
//@Composable
//fun SharedTransitionScope.AccountSwipeableCards(
//    modifier: Modifier = Modifier,
//    accounts: List<AccountEntity>,
//    accountViewModel: AccountScreenViewModel,
//    themeColors: ColorScheme,
//    onAccountReordered: (List<AccountEntity>) -> Unit = {},
//    navController: NavController,
//    animatedVisibilityScope: AnimatedVisibilityScope,
//) {
//    Box(
//        modifier
//            .padding(vertical = 32.dp)
//            .fillMaxWidth(),
//        contentAlignment = Alignment.TopCenter
//    ) {
//        if (accounts.isNotEmpty()) {
//            // Rendering in reverse order so first items appear on top
//            accounts.asReversed().forEachIndexed { idx, account ->
//                key(account.id) {
//                    SwipeableAccountCard(
//                        order = accounts.size - idx - 1, // Adjust order calculation
//                        totalCount = accounts.size,
//                        account = account,
//                        accountViewModel = accountViewModel,
//                        themeColors = themeColors,
//                        onMoveToBack = {
//                            // Update the order of accounts - append to the end instead of front
//                            val reorderedAccounts = (accounts - account) + listOf(account)
//                            onAccountReordered(reorderedAccounts)
//                        },
//                        onClick = {
//                            // Set the consistent ID for navigation
//                            navController.currentBackStackEntry?.savedStateHandle?.set(
//                                "accountId",
//                                account.id
//                            )
//                            navController.navigate(NavGraph.ACCOUNT_DETAILS){launchSingleTop = true}
//                        },
//                        modifier = Modifier.sharedBounds(
//                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
//                                contentScale = ContentScale.Inside
//                            ),
//                            sharedContentState = rememberSharedContentState(
//                                key = "${ACCOUNT_CARD_KEY_PREFIX}${account.id}"
//                            ),
//                            animatedVisibilityScope = animatedVisibilityScope
//                        )
//                    )
//                }
//            }
//        } else{
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator(color = themeColors.primary)
//            }
//        }
//    }
//}
//
//
//@Composable
//fun SwipeableAccountCard(
//    order: Int,
//    totalCount: Int, // should use it somewhere
//    account: AccountEntity,
//    accountViewModel: AccountScreenViewModel,
//    themeColors: ColorScheme,
//    onMoveToBack: () -> Unit,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val animatedScale by animateFloatAsState(
//        targetValue = 1f - (order) * 0.05f,
//    )
//    val animatedYOffset by animateDpAsState(
//        targetValue = (order * -12).dp,
//    )
//    Box(
//        modifier = modifier
//            .offset { IntOffset(x = 0, y = animatedYOffset.roundToPx()) }
//            .graphicsLayer {
//                scaleX = animatedScale
//                scaleY = animatedScale
//            }
//            .swipeWithClickable(
//                onClick = onClick,
//                onSwipeToBack = onMoveToBack
//            )
//
//    ) {
//        AccountCardLayout(
//            accountViewModel = accountViewModel,
//            item = account,
//            themeColors = themeColors,
//            notAllowSetAccountAsMain = true
//        )
//    }
//}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AccountSwipeableCards(
    modifier: Modifier = Modifier,
    accounts: List<AccountEntity>,
    setAccountAsMain: (AccountEntity) -> Unit,
    accountUiState: AccountScreenState,
    themeColors: ColorScheme,
    onAccountReordered: (List<AccountEntity>) -> Unit = {},
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Box(
        modifier
            .padding(vertical = 32.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (accounts.isNotEmpty()) {
            // Rendering in reverse order so first items appear on top
            accounts.asReversed().forEachIndexed { idx, account ->
                key(account.id) {
                    SwipeableAccountCard(
                        order = accounts.size - idx - 1, // Adjust order calculation
//                        totalCount = accounts.size,
                        account = account,
                        themeColors = themeColors,
                        onMoveToBack = {
                            // Update the order of accounts - append to the end instead of front
                            val reorderedAccounts = (accounts - account) + listOf(account)
                            onAccountReordered(reorderedAccounts)
                        },
                        onClick = {
                            // Set the consistent ID for navigation
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "accountId",
                                account.id
                            )
                            navController.navigate(NavGraph.ACCOUNT_DETAILS) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.sharedBounds(
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                contentScale = ContentScale.Inside
                            ),
                            sharedContentState = rememberSharedContentState(
                                key = "${ACCOUNT_CARD_KEY_PREFIX}${account.id}"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        setAccountAsMain = setAccountAsMain,
                        accountUiState = accountUiState
                    )
                }
            }
        } else{
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = themeColors.primary)
            }
        }
    }
}


@Composable
fun SwipeableAccountCard(
    order: Int,
//    totalCount: Int, // should use it somewhere
    account: AccountEntity,
    setAccountAsMain: (AccountEntity) -> Unit,
    accountUiState: AccountScreenState,
    themeColors: ColorScheme,
    onMoveToBack: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f - (order) * 0.05f,
    )
    val animatedYOffset by animateDpAsState(
        targetValue = (order * -12).dp,
    )
    Box(
        modifier = modifier
            .offset { IntOffset(x = 0, y = animatedYOffset.roundToPx()) }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .swipeWithClickable(
                onClick = onClick,
                onSwipeToBack = onMoveToBack
            )

    ) {
        AccountCardLayout(
            accountUiState = accountUiState,
            setAccountAsMain = setAccountAsMain,
            item = account,
            themeColors = themeColors,
            notAllowSetAccountAsMain = true,

            )
    }
}

fun Modifier.swipeWithClickable(
    onClick: () -> Unit,
    onSwipeToBack: () -> Unit
): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    var leftSide by remember { mutableStateOf(true) }
    var clearedHurdle by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }
    var processingSwipe by remember { mutableStateOf(false) }

    pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                if (!processingSwipe) {
                    isPressed = true
                    pressStartTime = System.currentTimeMillis()
                }
            },
            onTap = {
                if (!processingSwipe) {
                    onClick()
                }
            }
        )
    }
        .pointerInput(Unit) {
            val decay = splineBasedDecay<Float>(this)
            coroutineScope {
                while (true) {
                    offsetY.stop()
                    val velocityTracker = VelocityTracker()
                    awaitPointerEventScope {
                        verticalDrag(awaitFirstDown().id) { change ->
                            // Mark that we're processing a swipe gesture
                            processingSwipe = true

                            val verticalDragOffset = offsetY.value + change.positionChange().y
                            val horizontalPosition = change.previousPosition.x
                            leftSide = horizontalPosition <= size.width / 2
                            val offsetXRatioFromMiddle = if (leftSide) {
                                horizontalPosition / (size.width / 2)
                            } else {
                                (size.width - horizontalPosition) / (size.width / 2)
                            }
                            val rotationalOffset = max(1f, (1f - offsetXRatioFromMiddle) * 4f)
                            launch {
                                offsetY.snapTo(verticalDragOffset)
                                rotation.snapTo(if (leftSide) rotationalOffset else -rotationalOffset)
                            }
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            if (change.positionChange() != Offset.Zero) change.consume()
                        }
                    }

                    val velocity = velocityTracker.calculateVelocity().y
                    val targetOffsetY = decay.calculateTargetValue(offsetY.value, velocity)

                    // Reset the swipe processing flag
                    processingSwipe = false

                    if (targetOffsetY.absoluteValue <= size.height) {
                        // Not enough velocity; Reset.
                        launch { offsetY.animateTo(targetValue = 0f, initialVelocity = velocity) }
                        launch { rotation.animateTo(targetValue = 0f, initialVelocity = velocity) }
                    } else {
                        // Enough velocity to fling the card to the back
                        val boomerangDuration = 600
                        val maxDistanceToFling = (size.height * 4).toFloat()
                        val maxRotations = 3
                        val easeInOutEasing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
                        val distanceToFling = min(
                            targetOffsetY.absoluteValue + size.height, maxDistanceToFling
                        )
                        val rotationToFling = min(
                            360f * (targetOffsetY.absoluteValue / size.height).roundToInt(),
                            360f * maxRotations
                        )
                        val rotationOvershoot = rotationToFling + 12f

                        val animationJobs = listOf(
                            launch {
                                rotation.animateTo(
                                    targetValue = if (leftSide) rotationToFling else -rotationToFling,
                                    initialVelocity = velocity,
                                    animationSpec = keyframes {
                                        durationMillis = boomerangDuration
                                        0f at 0 using easeInOutEasing
                                        (if (leftSide) rotationOvershoot else -rotationOvershoot) at boomerangDuration - 50 using LinearOutSlowInEasing
                                        (if (leftSide) rotationToFling else -rotationToFling) at boomerangDuration
                                    }
                                )
                                rotation.snapTo(0f)
                            },
                            launch {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    initialVelocity = velocity,
                                    animationSpec = keyframes {
                                        durationMillis = boomerangDuration
                                        -distanceToFling at (boomerangDuration / 2) using easeInOutEasing
                                        40f at boomerangDuration - 70
                                    }
                                ) {
                                    if (value <= -size.height * 2 && !clearedHurdle) {
                                        onSwipeToBack()
                                        clearedHurdle = true
                                    }
                                }
                            }
                        )
                        animationJobs.joinAll()
                        clearedHurdle = false
                    }
                }
            }
        }
        .offset { IntOffset(0, offsetY.value.roundToInt()) }
        .graphicsLayer {
            transformOrigin = TransformOrigin.Center
            rotationZ = rotation.value
        }
}