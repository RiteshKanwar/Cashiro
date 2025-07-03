package com.ritesh.cashiro.presentation.ui.extras.components.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.presentation.ui.theme.iosFont

// Search Box Composable used in Category icon selection bottomSheet
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    label: String,
) {
    val themeColors = MaterialTheme.colorScheme
    TextField(
        value =  searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(
            text = label,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = iosFont,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()) },
        modifier = modifier
            .fillMaxWidth()
            .background(themeColors.surfaceBright, shape = RoundedCornerShape(15.dp)),
        singleLine = true,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = iosFont, fontSize = 16.sp),
        colors = TextFieldDefaults.colors(
            unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(alpha = 0.5f),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedLabelColor = themeColors.inverseSurface
        )
    )
}