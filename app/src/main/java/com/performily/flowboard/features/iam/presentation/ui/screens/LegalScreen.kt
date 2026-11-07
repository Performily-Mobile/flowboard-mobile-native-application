package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel
import androidx.compose.runtime.collectAsState


@Composable
fun LegalScreen(onBack: () -> Unit) {
    var privacy by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        TextButton(onClick = onBack) { Text("‹  Términos y privacidad") }
        Row { TextButton(onClick = { privacy = false }) { Text("Términos del servicio") }; TextButton(onClick = { privacy = true }) { Text("Privacidad") } }
        Text(if (privacy) "Política de privacidad" else "Términos del servicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Referencia de diseño: 01/09/2026", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        if (privacy) {
            LegalParagraph("Datos que tratamos", "Tu ficha laboral, asistencia, solicitudes, beneficios y boletas, registrados por tu organización.")
            LegalParagraph("Quién puede verlos", "Solo tú y el personal de Recursos Humanos autorizado, de acuerdo con tus permisos.")
            LegalParagraph("Marco legal", "Ley N.° 29733, Ley de Protección de Datos Personales, y su reglamento.")
            LegalParagraph("En tu dispositivo", "El token de sesión se guarda cifrado usando Android Keystore.")
        } else LegalParagraph("Uso del servicio", "Flowboard facilita la consulta de información laboral, sujeto a los permisos asignados por tu organización. La versión legal definitiva debe ser aprobada por la organización.")
    }
}

@Composable private fun LegalParagraph(title: String, detail: String) { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp)); Text(detail, modifier = Modifier.padding(top = 8.dp)) }

