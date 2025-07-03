package com.ritesh.cashiro.presentation.ui.extras.components.extras

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@Composable
fun StyledCurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default, // Disable default keyboard
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var showNumberPad by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = { newValue ->
                // Ensure that the value doesn't include "$" or allow the cursor before it
                val cleanValue = newValue.removePrefix("$")
                if (cleanValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    onValueChange(cleanValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showNumberPad = true }, // Open number pad on click
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText,
            isError = isError,
            visualTransformation = if (isFocused) CurrencyVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            shape = shape,
            colors = colors,
            interactionSource = interactionSource,
        )

        // Show custom number pad when TextField is focused
        if (showNumberPad) {
            CustomNumberPadOutlinedTextField(
                intervalInput = if (value.isEmpty()) 0 else value.toInt(),
                onValueChange = { newValue ->
                    onValueChange(newValue)
                },
                onDismiss = { showNumberPad = false } // Close number pad on dismiss
            )
        }
    }
}
class CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        // Prefix "$" to the input text
        val transformedText = AnnotatedString("$${text.text}")
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Add 1 to the offset to account for the "$" prefix
                return offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Subtract 1 to map the cursor back to the correct position
                return (offset - 1).coerceAtLeast(0)
            }
        }
        return TransformedText(transformedText, offsetMapping)
    }
}

@Composable
fun CustomNumberPadOutlinedTextField(
    intervalInput: Int,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit // Callback to close the number pad
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // OutlinedTextField with custom interaction
        OutlinedTextField(
            value = if (intervalInput == 0) "" else intervalInput.toString(),
            onValueChange = {}, // Value changes are handled by the number pad
            readOnly = true,
            label = {
                Text(
                    text = "Value",
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f),
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(15.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Number Pad
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "⌫")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                when (key) {
                                    "⌫" -> {
                                        val updatedValue = if (intervalInput.toString().isNotEmpty()) intervalInput.toString().dropLast(1) else "0"
                                        onValueChange(updatedValue)
                                    }
                                    "Clear" -> onValueChange("0")
                                    else -> onValueChange(intervalInput.toString() + key)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (key) {
                                    "Clear" -> MaterialTheme.colorScheme.onError
                                    "⌫" -> Macchiato_Peach.copy(0.5f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                contentColor = when (key) {
                                    "Clear" -> Color.White
                                    "⌫" -> Color.White
                                    else -> MaterialTheme.colorScheme.inverseSurface
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .size(65.dp)
                                .padding(5.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                text = key,
                                fontFamily = iosFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        // Dismiss Button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(text = "Done", fontFamily = iosFont)
        }
    }
}