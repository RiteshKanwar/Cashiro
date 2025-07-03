package com.ritesh.cashiro.presentation.ui.extras.components.extras

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.features.transactions.TransactionFilterState
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeDefaults.tint
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CustomTitleTopAppBar(
    modifier: Modifier = Modifier,
    scrollBehaviorSmall: TopAppBarScrollBehavior,
    scrollBehaviorLarge: TopAppBarScrollBehavior,
    title: String,
    previousScreenTitle: String = "",
    onBackClick : () -> Unit = {},
    hasBackButton: Boolean = false,
    isInSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onSearchButtonClick: () -> Unit = {},
    onFilterButtonClick: () -> Unit = {},
    hasFilterButton: Boolean = false,
    filterState: TransactionFilterState? = null,
    greetingCard: @Composable () -> Unit = {},
    profilePhoto: @Composable () -> Unit = {},
    hazeState: HazeState = HazeState(),
) {
    val collapsedFraction = scrollBehaviorLarge.state.collapsedFraction
    val displayTitle = remember { mutableStateOf(previousScreenTitle.ifEmpty { title }) }

    Box {
        // LargeTopAppBar
        LargerTopAppBar(
            scrollBehaviorLarge = scrollBehaviorLarge ,
            title = title,
            displayTitle = displayTitle,
            onBackClick  = onBackClick,
            hasBackButton = hasBackButton,
            isInSelectionMode = isInSelectionMode,
            selectedCount = selectedCount,
            onClearSelection = onClearSelection ,
            onDeleteSelected = onDeleteSelected,
            onSelectAll = onSelectAll,
            onEditClick = onEditClick,
            collapsedFraction = collapsedFraction,
            greetingCard = greetingCard,
            hazeState = hazeState,
            themeColors = MaterialTheme.colorScheme
        )

        // Regular TopAppBar
        RegularTopAppBar(
            scrollBehaviorSmall = scrollBehaviorSmall,
            title = title,
            onBackClick  = onBackClick,
            hasBackButton = hasBackButton,
            isInSelectionMode = isInSelectionMode,
            onEditClick = onEditClick,
            onSearchButtonClick = onSearchButtonClick,
            onFilterButtonClick = onFilterButtonClick,
            hasFilterButton = hasFilterButton,
            filterState = filterState,
            collapsedFraction = collapsedFraction,
            profilePhoto = profilePhoto ,
            modifier = modifier,
            hazeState = hazeState
        )

        RegularTopAppBarInSelectionMode(
            scrollBehaviorSmall = scrollBehaviorSmall ,
            hasBackButton = hasBackButton ,
            isInSelectionMode= isInSelectionMode,
            selectedCount = selectedCount ,
            onClearSelection = onClearSelection ,
            onDeleteSelected = onDeleteSelected ,
            onSelectAll = onSelectAll,
            collapsedFraction = collapsedFraction
        )
    }
}


@Composable
private fun Modifier.animatedOffsetModifier(
    hasBackButton: Boolean,
    isInSelectionMode: Boolean,
    isProfileScreen: Boolean = false,
    isTransactionScreen : Boolean = false,
): Modifier {
    // Define the target offset based on conditions
    val targetOffsetX = when {
        hasBackButton && isProfileScreen-> 0.dp
        isTransactionScreen -> (0).dp
        hasBackButton -> (-26).dp
        isInSelectionMode -> 0.dp
        else -> (-10).dp
    }

    // Convert to pixels for animation
    val density = LocalDensity.current
    val targetOffsetXPx = with(density) { targetOffsetX.toPx() }

    val transition = updateTransition(
        targetState = Triple(hasBackButton, isInSelectionMode, targetOffsetXPx),
        label = "offsetTransition"
    )

    val animatedOffsetX by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "offsetX"
    ) { (_, _, offset) -> offset }

    // Apply offset directly as a float value instead of rounding to Int
    return this
        .fillMaxWidth()
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                // Use the exact float value for positioning
                placeable.placeRelative(x = animatedOffsetX.toInt(), y = 0)
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeApi::class)
@Composable
private fun LargerTopAppBar(
    modifier: Modifier = Modifier,
    scrollBehaviorLarge: TopAppBarScrollBehavior,
    title: String,
    displayTitle: MutableState<String>,
    onBackClick : () -> Unit = {},
    hasBackButton: Boolean = false,
    isInSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onEditClick: () -> Unit = {},
    collapsedFraction: Float,
    greetingCard: @Composable () -> Unit = {} ,
    hazeState: HazeState,
    themeColors: ColorScheme,

){
    LargeTopAppBar(
        title = {
            TitleForLargeTopAppBar(
                title = title,
                isInSelectionMode = isInSelectionMode,
                selectedCount = selectedCount,
                modifier = modifier ,
                greetingCard = greetingCard,
            )
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor =  Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        navigationIcon = {
            NavigationForLargeTopAppBar(
                isInSelectionMode = isInSelectionMode,
                onClearSelection = onClearSelection,
                displayTitle = displayTitle,
                onBackClick = onBackClick,
                hasBackButton = hasBackButton,
            )
        },
        actions = {
            // Fixed edit button for profile screen
            ActionForLargeTopAppBar(
                title = title,
                isInSelectionMode = isInSelectionMode,
                onDeleteSelected = onDeleteSelected,
                onSelectAll = onSelectAll,
                onEditClick = onEditClick,
            )
        },
        collapsedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
        expandedHeight = if (title == "Home") 150.dp else 110.dp,
        scrollBehavior = scrollBehaviorLarge,
        modifier = Modifier
            .fillMaxWidth()
            .hazeChild(
                state = hazeState,
                block = {
                    this.inputScale = HazeInputScale.Auto
                    style = HazeDefaults.style(
                        backgroundColor = Color.Transparent,
                        tint = tint(backgroundColor),
                        blurRadius = 10.dp,
                        noiseFactor = -1f,)
                    progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
                }
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        themeColors.background,
                        Color.Transparent
                    )
                )
            )

            .alpha(1f - collapsedFraction)
    )
}

@Composable
private fun TitleForLargeTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    isInSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    greetingCard: @Composable () -> Unit = {},
){
    BlurredAnimatedVisibility(
        visible = isInSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Text(
            text = "$selectedCount selected",
            fontSize = 28.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            textAlign =  TextAlign.Start ,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        )
    }
    BlurredAnimatedVisibility(
        visible = !isInSelectionMode && title != "Home",
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            textAlign =  TextAlign.Start ,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 10.dp)
        )
    }

    if (!isInSelectionMode && title == "Home"){
        greetingCard()
    }
}

@Composable
private fun NavigationForLargeTopAppBar(
    modifier: Modifier = Modifier,
    isInSelectionMode: Boolean = false,
    onClearSelection: () -> Unit = {},
    displayTitle: MutableState<String>,
    onBackClick : () -> Unit = {},
    hasBackButton: Boolean = false,
){
    BlurredAnimatedVisibility (
        visible = isInSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        // Show close button when in selection mode
        IconButton(
            onClick = onClearSelection,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 15.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary),
        ) {
            Row(modifier = modifier
                .clickable { onClearSelection() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Back Button",
                )
                Text(
                    text = "Clear All",
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
                )
            }
        }
    }
    BlurredAnimatedVisibility(
        visible = hasBackButton && !isInSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Row(
            modifier = modifier
                .animateContentSize()
                .padding(start = 15.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_mini_left_bulk),
                contentDescription = "Back Button",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            AnimatedContent(
                targetState = displayTitle.value,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth }
                    ) togetherWith fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth }
                    )
                },
                label = "",
            ) { targetTitle ->
                Text(
                    text = targetTitle,
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun ActionForLargeTopAppBar(
    title: String,
    isInSelectionMode: Boolean = false,
    onDeleteSelected: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onEditClick: () -> Unit = {},
){
    BlurredAnimatedVisibility(
        visible = !isInSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        if (title == "Profile") {
            Row(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable(
                        onClick = onEditClick,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,

                        ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit_bulk),
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Edit",
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                )

            }
        }

        if (title == "Home"){
            Box(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .size(40.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape =CircleShape)
                    .clickable(
                        onClick = onEditClick,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,

                        ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_bulk),
                    contentDescription = "More option",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    BlurredAnimatedVisibility(
        visible = isInSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Select All button
            IconButton(
                onClick = onSelectAll,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.inverseSurface
                ),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.select_all),
                    contentDescription = "All Selected",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onDeleteSelected,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor =Color.White
                ),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onError,
                        shape = RoundedCornerShape(15.dp)
                    )
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    contentDescription = "Delete Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeApi::class)
@Composable
private fun RegularTopAppBar(
    modifier: Modifier = Modifier,
    scrollBehaviorSmall: TopAppBarScrollBehavior,
    title: String,
    onBackClick : () -> Unit = {},
    hasBackButton: Boolean = false,
    isInSelectionMode: Boolean = false,
    onEditClick: () -> Unit = {},
    onSearchButtonClick: () -> Unit = {},
    onFilterButtonClick: () -> Unit = {},
    hasFilterButton: Boolean = false,
    filterState: TransactionFilterState? = null,
    profilePhoto: @Composable () -> Unit = {},
    collapsedFraction: Float,
    hazeState: HazeState,
){
    BlurredAnimatedVisibility(
        visible = collapsedFraction > 0.01f && !isInSelectionMode,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val isProfileScreen = title== "Profile"
        val isTransactionScreen = title == "Transactions"
        val isSearchTransactionScreen = title == "Search Transactions"

        TopAppBar(
            title = {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    textAlign =  TextAlign.Center,
                    modifier = Modifier.animatedOffsetModifier(
                        hasBackButton = hasBackButton,
                        isInSelectionMode = isInSelectionMode,
                        isProfileScreen = isProfileScreen,
                        isTransactionScreen = isTransactionScreen
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor =  Color.Transparent
            ),
            navigationIcon = {
                BlurredAnimatedVisibility (
                    visible = hasBackButton,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.inverseSurface,
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_mini_left_bulk),
                            contentDescription = "Back Button",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                AnimatedVisibility (isTransactionScreen ||hasFilterButton) {
                    Box(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(onClick = onFilterButtonClick)
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show indicator dot if filters are active
                        if (filterState?.isFilterActive() == true) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 6.dp)
                            )
                        }

                        Icon(
                            painter = painterResource(R.drawable.filter_bulk), // You'll need this icon
                            contentDescription = "Filter transactions",
                            tint = if (filterState?.isFilterActive() == true)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (title == "Home"){
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp)
                    ){
                        profilePhoto()
                    }
                }
            },
            actions = {
                // Add edit button for collapsed view as well
                if (title == "Profile") {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(
                                onClick = onEditClick
                            )
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.edit_bulk),
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Transaction screen buttons (search and filter)
                if (isTransactionScreen || isSearchTransactionScreen) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        // Filter button (show if hasFilterButton is true)
                        if (isSearchTransactionScreen && hasFilterButton) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable(onClick = onFilterButtonClick)
                                    .size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Show indicator dot if filters are active
                                if (filterState?.isFilterActive() == true) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-6).dp, y = 6.dp)
                                    )
                                }

                                Icon(
                                    painter = painterResource(R.drawable.filter_bulk), // You'll need this icon
                                    contentDescription = "Filter transactions",
                                    tint = if (filterState?.isFilterActive() == true)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.inverseSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Search button (only show for transaction screen, not search screen)
                        if (isTransactionScreen) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable(onClick = onSearchButtonClick)
                                    .size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search_bulk),
                                    contentDescription = "Search transactions",
                                    tint = MaterialTheme.colorScheme.inverseSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (title == "Home"){
                    Box(
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .size(40.dp)
                            .background(color = MaterialTheme.colorScheme.surface, shape =CircleShape)
                            .clickable(
                                onClick = onEditClick,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_bulk),
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            scrollBehavior = scrollBehaviorSmall,
            expandedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
            modifier = Modifier
                .fillMaxWidth()
                .hazeChild(
                    state = hazeState,
                    block = {
                        style = HazeDefaults.style(
                            backgroundColor = Color.Transparent,
                            blurRadius = 10.dp,
                            noiseFactor = -1f,)
                        progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
                    }
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                )
                .alpha(collapsedFraction)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegularTopAppBarInSelectionMode(
    scrollBehaviorSmall: TopAppBarScrollBehavior,
    hasBackButton: Boolean = false,
    isInSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    collapsedFraction: Float
) {
    BlurredAnimatedVisibility(
        visible = isInSelectionMode,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "$selectedCount selected",
                    fontSize = 18.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    textAlign =  TextAlign.Start ,
                    modifier = Modifier.padding(start = 24.dp).animatedOffsetModifier(
                        hasBackButton = hasBackButton,
                        isInSelectionMode = isInSelectionMode,
                    )
                )

            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor =  MaterialTheme.colorScheme.background
            ),
            navigationIcon = {
                IconButton(
                    onClick = onClearSelection,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Selection",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Select All button (collapsed view)
                    IconButton(
                        onClick = onSelectAll,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.inverseSurface
                        ),
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.select_all),
                            contentDescription = "Select All",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteSelected,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent,
                            contentColor =Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onError,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.delete_bulk),
                            contentDescription = "Delete Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            scrollBehavior = scrollBehaviorSmall,
            expandedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(collapsedFraction)
        )
    }
}