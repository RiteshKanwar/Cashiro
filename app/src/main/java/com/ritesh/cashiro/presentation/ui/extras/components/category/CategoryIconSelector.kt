package com.ritesh.cashiro.presentation.ui.extras.components.category

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.domain.utils.CategoryIconItem
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.text.contains

@Composable
fun CategoryIconSelector(
    selectedIconId: Int?,
    onIconSelected: (Int) -> Unit,
) {
    var localSelectedIconId by remember(selectedIconId) { mutableIntStateOf(selectedIconId ?: 0) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val filteredIcons = icons.filter {
        it.name.contains(searchQuery.text, ignoreCase = true) ||
                it.category.contains(searchQuery.text, ignoreCase = true)
    }

    Column(modifier = Modifier
        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            label = "SEARCH"
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Animate the filtered icons
        AnimatedContent(
            targetState = filteredIcons,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }, label = "Filtered Icon animated"
        ) { iconsToDisplay ->
            CategoryIconFlowLayout(
                icons = iconsToDisplay,
                selectedIconId = selectedIconId,
                onIconSelected = { iconId ->
                    localSelectedIconId = iconId
                    onIconSelected(iconId)
                },
            )
        }
    }
}



// Icon Flow Layout Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CategoryIconFlowLayout(
    icons: List<CategoryIconItem>,
    selectedIconId: Int?,
    onIconSelected: (Int) -> Unit,
) {
    val themeColors = MaterialTheme.colorScheme
    val groupedIcons = icons.groupBy { it.category }

    LazyColumn {
        groupedIcons.forEach { (category, iconsInCategory) ->
            stickyHeader(key = "$category header") {
                Text(
                    text = category,
                    color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                        .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                themeColors.background,
                                themeColors.background,
                                Color.Transparent,
                            )
                        )
                    ).padding(10.dp)
                )
            }
            item(key = category){
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                ) {
                    iconsInCategory.forEach { icon ->
                        CategoryIconItemView(
                            icon = icon,
                            isSelected = icon.id == selectedIconId,
                            onClick = { onIconSelected(icon.id) }
                        )
                    }
                }
            }
        }
    }
}

// Icon Item View Composable
@Composable
private fun CategoryIconItemView(
    icon: CategoryIconItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val themeColors = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "Selected Glow animation")
    val animatedColors = infiniteTransition.animateColor(
        initialValue = Color.Transparent,
        targetValue = Color.Transparent,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000 // Total duration for the animation cycle
                Latte_Maroon at 0 using LinearEasing
                Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
                Macchiato_Peach at 2000 using LinearEasing
            },
            repeatMode = RepeatMode.Reverse
        ), label = "Selected Glow animation")
    Box(
        modifier = Modifier
            .size(64.dp)
            .then(
                if (isSelected){
                    Modifier.shadow(25.dp, RoundedCornerShape(20.dp),spotColor = animatedColors.value, ambientColor = animatedColors.value)}
                else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = { onClick() })
            .background(color = themeColors.surfaceVariant, shape = RoundedCornerShape(20.dp))
            .then(
                if (isSelected){
                    Modifier.border(width = 2.dp, shape = RoundedCornerShape(20.dp), color =  animatedColors.value)}
                else {
                    Modifier
                }
            )
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon.resourceId),
            contentDescription = icon.name,
            modifier = Modifier.size(50.dp)
        )
    }
}