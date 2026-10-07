package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

internal object RequestIcons {

    val Sync: ImageVector by lazy {
        icon(
            "sync",
            "M12,4V1L8,5l4,4V6c3.31,0 6,2.69 6,6 0,1.01 -0.25,1.97 -0.7,2.8l1.46,1.46C19.54,15.03 20,13.57 20,12c0,-4.42 -3.58,-8 -8,-8zM12,18c-3.31,0 -6,-2.69 -6,-6 0,-1.01 0.25,-1.97 0.7,-2.8L5.24,7.74C4.46,8.97 4,10.43 4,12c0,4.42 3.58,8 8,8v3l4,-4 -4,-4v3z"
        )
    }

    val Description: ImageVector by lazy {
        icon(
            "description",
            "M8,16h8v2H8zM8,12h8v2H8zM14,2H6c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.89,2 1.99,2H18c1.1,0 2,-0.9 2,-2V8l-6,-6zM18,20H6V4h7v5h5v11z"
        )
    }

    val Delete: ImageVector by lazy {
        icon(
            "delete",
            "M16,9v10H8V9h8m-1.5,-6h-5l-1,1H5v2h14V4h-3.5l-1,-1zM18,7H6v12c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7z"
        )
    }

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        )
            .addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
            .build()
}
