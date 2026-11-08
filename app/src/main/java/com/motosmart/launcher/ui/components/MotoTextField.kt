package com.motosmart.launcher.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.motosmart.launcher.ui.theme.LocalMotoPalette

@Composable
fun MotoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    imeAction: ImeAction = ImeAction.Done,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val palette = LocalMotoPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = singleLine,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            }
        },
        trailingIcon = trailingIcon,
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = keyboardOptions.copy(imeAction = imeAction),
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = palette.accent,
            unfocusedBorderColor = palette.outline,
            focusedContainerColor = palette.panelHigh,
            unfocusedContainerColor = palette.panelHigh,
            cursorColor = palette.accent,
            focusedTextColor = palette.textPrimary,
            unfocusedTextColor = palette.textPrimary,
            focusedPlaceholderColor = palette.textMuted,
            unfocusedPlaceholderColor = palette.textMuted,
            focusedLeadingIconColor = palette.accent,
            unfocusedLeadingIconColor = palette.textMuted,
            focusedTrailingIconColor = palette.textSecondary,
            unfocusedTrailingIconColor = palette.textSecondary
        )
    )
}
