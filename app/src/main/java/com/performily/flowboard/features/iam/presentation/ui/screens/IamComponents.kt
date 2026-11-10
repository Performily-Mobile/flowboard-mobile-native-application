package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Primary
import com.performily.flowboard.core.designsystem.theme.Spacing

/** Brand color of the IAM screens; it is the primary color of the theme. */
internal val brand: Color = Primary

/**
 * Password text field with a button that shows or hides the text.
 *
 * @param value current text.
 * @param onChange called with the new text.
 * @param label label of the field.
 * @param error true to show the field in its error state.
 * @param supportingError message shown below the field while [error] is true.
 */
@Composable
fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    error: Boolean = false,
    supportingError: String? = null
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        isError = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        supportingText = if (error && !supportingError.isNullOrBlank()) {
            { Text(supportingError) }
        } else {
            null
        }
    )
}

/**
 * Error banner of the IAM screens, with the same look as the error banners of the other features.
 *
 * @param message error to show.
 */
@Composable
fun ErrorBanner(message: String) {
    IamBanner(
        message = message,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        showIcon = true
    )
}

/**
 * Confirmation banner of the IAM screens, for example after the password was changed.
 *
 * @param message confirmation to show.
 */
@Composable
fun SuccessBanner(message: String) {
    IamBanner(
        message = message,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        showIcon = false
    )
}

/** Tonal banner shared by [ErrorBanner] and [SuccessBanner]. */
@Composable
private fun IamBanner(message: String, containerColor: Color, contentColor: Color, showIcon: Boolean) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIcon) {
                Icon(FlowboardIcons.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}
