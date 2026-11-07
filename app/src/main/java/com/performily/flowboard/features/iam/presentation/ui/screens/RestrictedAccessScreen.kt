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
fun RestrictedAccessScreen(onMyProfile: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Lock, null, tint = brand, modifier = Modifier.size(75.dp))
        Spacer(Modifier.height(24.dp))
        Text("No tienes acceso a esta información", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Solo puedes consultar tu propia información laboral. El acceso a otros colaboradores está restringido por rol.", textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onMyProfile, colors = ButtonDefaults.buttonColors(containerColor = brand)) { Text("Ir a mi perfil") }
        TextButton(onClick = onHome) { Text("Volver al inicio") }
    }
}
