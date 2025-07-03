package com.ritesh.cashiro.presentation.ui.extras.components.extras


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogs(
    isDatePickerDialogOpen: Boolean,
    onConfirm: ()-> Unit,
    onClose: ()-> Unit,
    datePickerState: DatePickerState
){
    AnimatedVisibility(isDatePickerDialogOpen){
        DatePickerDialog(
            onDismissRequest = {onClose()},
            confirmButton = {
                TextButton(
                    onClick = { onConfirm() },
                    modifier = Modifier
                        .shadow(5.dp, RoundedCornerShape(15.dp), spotColor = MaterialTheme.colorScheme.primary, ambientColor = Color.Transparent),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(15.dp)
                ){
                    Text(
                        text = "Okay",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = TextStyle(
                            fontFamily = iosFont,
                        )

                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onClose() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    shape = RoundedCornerShape(15.dp)
                ){
                    Text(
                        text = "Cancel",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        )
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness =  Spring.StiffnessLow))
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    headlineContentColor = MaterialTheme.colorScheme.inverseSurface,
                    subheadContentColor= MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    weekdayContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    yearContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    currentYearContentColor = MaterialTheme.colorScheme.inverseSurface,
                    navigationContentColor = MaterialTheme.colorScheme.inverseSurface,
                    containerColor = MaterialTheme.colorScheme.background,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    selectedYearContentColor = Color.White,
                    selectedDayContentColor = Color.White,
                    disabledSelectedDayContentColor = MaterialTheme.colorScheme.inverseSurface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    disabledSelectedDayContainerColor = MaterialTheme.colorScheme.onBackground,
                    dayContentColor = MaterialTheme.colorScheme.inverseSurface,
                ),
            )
        }
    }
}

