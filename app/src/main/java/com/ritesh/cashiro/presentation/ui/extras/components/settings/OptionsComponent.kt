package com.ritesh.cashiro.presentation.ui.extras.components.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.theme.ErrorColor

@Composable
fun OptionsComponent(
    showIcon: Boolean = false,
    @DrawableRes iconResID: Int = 0,
    iconBackgroundColor: Color = Color.Transparent,
    label: String,
    hasArrow: Boolean = false,
    isDestructive: Boolean = false,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    isSingleItem: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit? = {}
) {
    val themeColors = MaterialTheme.colorScheme
    val textColor = if (isDestructive) Color.White else themeColors.inverseSurface
    val arrowColor = themeColors.inverseSurface.copy(0.5f)

    // Define the corner shape based on position (similar to ExchangeRateItem)
    val cornerShape = when {
        isFirstItem -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 5.dp,
            bottomEnd = 5.dp
        )
        isLastItem -> RoundedCornerShape(
            topStart = 5.dp,
            topEnd = 5.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
        isSingleItem -> RoundedCornerShape(16.dp)
        else -> RoundedCornerShape(5.dp) // Middle items have small rounded corners
    }

    ListItem(
        headlineContent = {
            Text(
                text = label,
                fontFamily = iosFont,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        },
        leadingContent = if (showIcon) {
            {
                Box(
                    modifier = Modifier
                        .background(
                            color = iconBackgroundColor,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = iconResID),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                }
            }
        } else null,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
                if (hasArrow) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_mini_right_bulk),
                        contentDescription = null,
                        tint = arrowColor
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isDestructive) ErrorColor.copy(0.6f) else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .padding(bottom = if (!isLastItem) 1.dp else 0.dp)
            .clip(cornerShape)
    )
}
