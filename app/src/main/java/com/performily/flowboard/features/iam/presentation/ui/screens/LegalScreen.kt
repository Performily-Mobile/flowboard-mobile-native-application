package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Terms of service and privacy policy, opened from the login and from the account settings.
 *
 * @param onBack called when the back button is tapped.
 */
@Composable
fun LegalScreen(onBack: () -> Unit) {
    var selectedTab by rememberSaveable { mutableIntStateOf(PRIVACY_TAB) }

    Scaffold(
        topBar = { FlowboardTopBar(title = "Términos y privacidad", onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.xs, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            FlowboardTabs(
                labels = listOf("Términos del servicio", "Privacidad"),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = if (selectedTab == PRIVACY_TAB) "Política de privacidad" else "Términos del servicio",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Última actualización: 01/09/2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedTab == PRIVACY_TAB) {
                LegalParagraph(
                    "Datos que tratamos",
                    "Tu ficha laboral, asistencia, solicitudes, beneficios y boletas, registrados por tu organización."
                )
                LegalParagraph(
                    "Quién puede verlos",
                    "Solo tú y el personal de Recursos Humanos autorizado, de acuerdo con tus permisos."
                )
                LegalParagraph(
                    "Marco legal",
                    "Ley N.° 29733, Ley de Protección de Datos Personales, y su reglamento."
                )
                LegalParagraph(
                    "En tu dispositivo",
                    "El token de sesión se guarda cifrado usando Android Keystore."
                )
            } else {
                LegalParagraph(
                    "Uso del servicio",
                    "Flowboard facilita la consulta de información laboral, sujeto a los permisos asignados por tu " +
                        "organización. La versión legal definitiva debe ser aprobada por la organización."
                )
            }
        }
    }
}

/**
 * One clause of the legal text: a title and its detail.
 *
 * @param title title of the clause.
 * @param detail text of the clause.
 */
@Composable
private fun LegalParagraph(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Index of the privacy tab, shown first. */
private const val PRIVACY_TAB = 1
