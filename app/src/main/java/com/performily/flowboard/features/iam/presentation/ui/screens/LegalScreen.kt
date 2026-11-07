package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val legalBackground = Color(0xFFF8F9FF)
private val legalText = Color(0xFF202124)
private val legalSecondary = Color(0xFF4A4E57)

@Composable
fun LegalScreen(onBack: () -> Unit) {
    var selectedTab by rememberSaveable { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(legalBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = legalSecondary
                )
            }
            Text(
                text = "Términos y privacidad",
                color = legalText,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = legalBackground,
            contentColor = brand,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = brand
                )
            },
            divider = { HorizontalDivider(color = Color(0xFFE1E4EA)) }
        ) {
            listOf("Términos del servicio", "Privacidad").forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            label,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    selectedContentColor = brand,
                    unselectedContentColor = legalSecondary,
                    modifier = Modifier.height(64.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 18.dp)
        ) {
            Text(
                text = if (selectedTab == 1) "Política de privacidad" else "Términos del servicio",
                style = MaterialTheme.typography.headlineMedium,
                color = legalText
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Última actualización: 01/09/2026",
                style = MaterialTheme.typography.bodyMedium,
                color = legalSecondary
            )
            Spacer(Modifier.height(14.dp))

            if (selectedTab == 1) {
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
                    "Flowboard facilita la consulta de información laboral, sujeto a los permisos asignados por tu organización. La versión legal definitiva debe ser aprobada por la organización."
                )
            }
        }
    }
}

@Composable
private fun LegalParagraph(title: String, detail: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = legalText,
        modifier = Modifier.padding(top = 16.dp)
    )
    Text(
        text = detail,
        style = MaterialTheme.typography.bodyLarge,
        color = legalSecondary,
        modifier = Modifier.padding(top = 8.dp)
    )
}
