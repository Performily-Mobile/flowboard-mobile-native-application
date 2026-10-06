package com.performily.flowboard.core.navigation

import kotlinx.serialization.Serializable

/*
 * Rutas de la estructura general de la app (barra inferior y "Más").
 * Las rutas marcadas como PENDIENTE muestran una pantalla temporal: cuando cada
 * bounded context se integre (Payroll, Request, Attendance...), se reemplazan
 * en TopLevelDestination por la ruta de su propio NavGraph.
 */

@Serializable
data object MoreRoute

// PENDIENTE: panel de RR.HH. (MA-18)
@Serializable
data object PanelRoute

// PENDIENTE: inicio del colaborador (MA-14)
@Serializable
data object HomeRoute

// PENDIENTE: bounded context Request
@Serializable
data object RequestsRoute

// PENDIENTE: bounded context Attendance
@Serializable
data object AttendanceRoute

// PENDIENTE: bounded context Payroll
@Serializable
data object PayslipsRoute

/** Pantalla temporal para opciones de "Más" que aún no están integradas. */
@Serializable
data class PendingFeatureRoute(val title: String)
