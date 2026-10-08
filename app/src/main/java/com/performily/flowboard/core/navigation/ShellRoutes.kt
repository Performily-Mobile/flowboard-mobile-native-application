package com.performily.flowboard.core.navigation

import kotlinx.serialization.Serializable

/** "Más" tab: entry point to the options that are not part of the main bar. */
@Serializable
data object MoreRoute

/** HR dashboard tab (MA-18). */
@Serializable
data object PanelRoute

/** Employee home tab (MA-14). Not integrated yet: it shows a temporary screen. */
@Serializable
data object HomeRoute

/** Requests tab of the Request bounded context. */
@Serializable
data object RequestsRoute

/** Attendance tab of the Attendance bounded context. */
@Serializable
data object AttendanceRoute

/** Payslips tab of the Payroll bounded context. */
@Serializable
data object PayslipsRoute

/**
 * Temporary screen for options of "Más" that are not integrated yet.
 *
 * @property title name of the option, shown as the screen title.
 */
@Serializable
data class PendingFeatureRoute(val title: String)
